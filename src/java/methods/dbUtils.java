/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class dbUtils
{
	private static DataSource datasource;
	static
	{
		try
		{
			datasource = (DataSource) new InitialContext().lookup("jdbc/consensus_rm_pool");
		}
		catch (NamingException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	public static Connection getConnection()
	{
		Connection conn = null;
		try
		{
			conn = datasource.getConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
	}

	

	public static void closeConnection(Connection conn)
	{
		try
		{
			if (conn != null && !conn.isClosed())
			{
				conn.close();
			}
		}
		catch (SQLException sqle)
		{
		}
	}
}

