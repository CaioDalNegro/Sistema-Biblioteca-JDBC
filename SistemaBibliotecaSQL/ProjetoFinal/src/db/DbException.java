package db;

public class DbException extends RuntimeException {//herda os métodos de RuntimeException
	
	private static final long serialVersionUID = 1L;
	
	public DbException(String msg) {//método construtor
		super(msg);
	}
}
