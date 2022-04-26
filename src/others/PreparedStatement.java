package others;

import java.util.List;

import clients.StartEmulation;

/*
 * This class simulate Statement entity
 */
public class PreparedStatement {
	public final String query;

	PreparedStatement(String query) {
		this.query = query;
	}

	public void setParameter(List<String> parameters) throws Exception {
	}

	public void executeQuery() throws Exception {
		Thread.sleep(StartEmulation.DATABASE_QUERY_TIME_MILLIS);
	}

	public void close() {

	}
}
