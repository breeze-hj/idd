package eastwind.idd.support;

/**
 * Created by jan.huang on 2018/4/11.
 */
public class State {

    protected String state;
    protected int level;

    public State(String state, int level) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
    
    public int getLevel() {
		return level;
	}
}
