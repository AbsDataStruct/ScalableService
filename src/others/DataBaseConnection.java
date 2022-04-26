package others;

/*
 * This class simulate database connection
 */
public class DataBaseConnection {

	public void connectDatabase(String connection) throws Exception {
	}

	public PreparedStatement prepareStatement(String query) throws Exception {
		return new PreparedStatement(query);
	}

	public void closeDatabase() throws Exception {
	}

}
