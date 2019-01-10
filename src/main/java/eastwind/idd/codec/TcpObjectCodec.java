package eastwind.idd.codec;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.idd.model.TcpObject;
import eastwind.idd.support.IddMagic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class TcpObjectCodec extends ByteToMessageCodec<TcpObject> {

	private static Logger LOGGER = LoggerFactory.getLogger(TcpObjectCodec.class);

	private static final int MAX_WINDOW_SIZE = 7 * 1024;

	private static ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> KryoFactory.newKryo());
	private static ThreadLocal<Output> OUTPUT = ThreadLocal.withInitial(() -> new Output(4096));
	private static ThreadLocal<Input> INPUT = ThreadLocal.withInitial(() -> new Input(4096));

	@Override
	protected void encode(ChannelHandlerContext ctx, TcpObject msg, ByteBuf out) throws Exception {
		LOGGER.debug("-->{}:{}", ctx.channel().remoteAddress(), msg);

		Kryo kryo = KRYO.get();
		Output output = OUTPUT.get();

		if (msg.args > 0) {
			Object[] data = null;
			if (msg.args == 1) {
				data = new Object[] { msg.data };
			} else {
				data = (Object[]) msg.data;
			}
			for (int j = 0; j < msg.args; j++) {
				writeObject(out, kryo, output, data[j]);
			}
		} else if (msg.args == -1) {
			Iterator<?> it = (Iterator<?>) msg.data;
			byte i = 0;
			for (; it.hasNext() && (out.readableBytes() < MAX_WINDOW_SIZE || i == 0) && i < Byte.MAX_VALUE; i++) {
				writeObject(out, kryo, output, it.next());
			}
			msg.args = i;
		}

		ByteBuf line = ctx.alloc().buffer();
		line.writeBytes(IddMagic.MAGIC);
		line.writeShort(0);
		int i = line.writerIndex();
		line.writeShort(0);
		output.setOutputStream(new ByteBufOutputStream(line));
		kryo.writeClassAndObject(output, msg);
		output.flush();
		line.setShort(i, line.writerIndex() - i);
		line.setShort(IddMagic.MAGIC.length, line.readableBytes() - IddMagic.MAGIC.length + out.readableBytes());

		output.clear();
		ctx.write(line);
	}

	private void writeObject(ByteBuf out, Kryo kryo, Output output, Object data) {
		int i = out.writerIndex();
		out.writeShort(0);
		output.setOutputStream(new ByteBufOutputStream(out));
		kryo.writeClassAndObject(output, data);
		output.flush();
		out.setShort(i, out.writerIndex() - i);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < IddMagic.MAGIC.length + 2) {
			return;
		}
		in.markReaderIndex();
		in.skipBytes(IddMagic.MAGIC.length);
		int len = in.readShort();
		if (in.readableBytes() < len - 2) {
			in.resetReaderIndex();
			return;
		}

		Kryo kryo = KRYO.get();
		Input input = INPUT.get();
		int i = in.readShort();
		input.setInputStream(new ByteBufInputStream(in, i - 2));
		TcpObject msg = (TcpObject) kryo.readClassAndObject(input);
		input.close();

		if (msg.args > 0) {
			Object[] data = new Object[msg.args];
			for (int j = 0; j < msg.args; j++) {
				i = in.readShort();
				input.setInputStream(new ByteBufInputStream(in, i - 2));
				Object obj = kryo.readClassAndObject(input);
				data[j] = obj;
				input.close();
			}
			if (msg.args == 1) {
				msg.data = data[0];
			} else {
				msg.data = data;
			}
		}
		LOGGER.debug("{}-->:{}", ctx.channel().remoteAddress(), msg);
		out.add(msg);
	}
}
