package com.skhynix.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import com.skhynix.base.BaseConnection;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Pair;
import com.skhynix.extern.Resourceable;
import com.skhynix.model.session.ASSessModel;
import com.skhynix.model.session.BaseSessModel;
import com.tibco.datagrid.BatchResult;
import com.tibco.datagrid.ColumnType;
import com.tibco.datagrid.Connection;
import com.tibco.datagrid.DataGrid;
import com.tibco.datagrid.DataGridException;
import com.tibco.datagrid.Event;
import com.tibco.datagrid.EventType;
import com.tibco.datagrid.GridMetadata;
import com.tibco.datagrid.ResultSet;
import com.tibco.datagrid.ResultSetMetadata;
import com.tibco.datagrid.Row;
import com.tibco.datagrid.RowSet;
import com.tibco.datagrid.Session;
import com.tibco.datagrid.Statement;
import com.tibco.datagrid.Table;
import com.tibco.datagrid.TableEventHandler;
import com.tibco.datagrid.TableListener;
import com.tibco.datagrid.TableMetadata;


/**
 * A sample program to illustrate the usage of the various APIs for accessing an ActiveSpaces Data Grid.
 *
 * The program assumes that a table has been configured that has a primary index that uses a column, called key,
 * of type long, and a second column, called value, of type string. By default the name of the table is t1, but
 * this can be changed by providing a different table name via the --tableName command line option.
 *
 * If the data grid is created by running the as-start (or as-start.bat) script that is provided with the installation
 * then the required table will be defined.
 */
public class ASRepository extends BaseConnection implements Resourceable {

	private static final ASRepository instance = new ASRepository();
	
	private static final String defaultServerUrl  = "http://localhost:8585";
	
	public ASRepository() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getDefaultServerUrl() {
		// TODO Auto-generated method stub
		return defaultServerUrl;
	}

	public static ASRepository getInstance() {
		return instance;
	}

    private Predicate<Object> CheckObject = obj -> Integer.class.isInstance(obj) || Long.class.isInstance(obj) || String.class.isInstance(obj);


    private enum MetadataType {
        GRID,
        TABLE
    }
    
    private enum OPERATION {
    	NONE("none") {
			@Override
			Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
				// TODO Auto-generated method stub
				return null;
			}
		},
    	GET("get") {
			@Override
			Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
				// TODO Auto-generated method stub
				return ASRepository.getInstance().getRow(table, keyName, key, defaultProperties);
			}
		},
    	PUT("put") {
			@Override
			Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
				// TODO Auto-generated method stub
				return ASRepository.getInstance().putRow(table, keyName, key, valueName, value, defaultProperties);
			}
		},
    	DELETE("delete") {
			@Override
			Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
				// TODO Auto-generated method stub
				return ASRepository.getInstance().deleteRow(table, keyName, key, defaultProperties);
			}
		},
    	UPDATE("update") {
			@Override
			Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
				// TODO Auto-generated method stub
				return ASRepository.getInstance().updateRow(table, keyName, key, valueName, value, defaultProperties);
			}
		};

    	private final String op;
    	private OPERATION(String op) {
    		this.op = op;
    	}
    	
    	public static OPERATION getType(String op) {
    		if(op.equals(GET.op)) return GET;
    		else if(op.equals(PUT.op)) return PUT;
    		else if(op.equals(DELETE.op)) return PUT;
    		else if(op.equals(UPDATE.op)) return UPDATE;
    		return NONE;
    	}
    	
    	abstract Object apply(Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties);
    	
    	public static Object applyOperation(OPERATION operation, Table table, String keyName, Object key, String valueName, String value, Properties defaultProperties) {
    		return operation.apply(table, keyName, key, valueName, value, defaultProperties);
    	}
    }
    	
    /** get/put/delete/update 함수를 수행하는 메인 함수 **/
    public Object runCommand(ASSessModel asClient, String tableName, String op, String keyName, Object key, String valueName, String value)
    {
    	return runCommand(asClient, tableName, OPERATION.getType(op), keyName, key, valueName, value);
    }

    public Object runCommand(ASSessModel asClient, String tableName, OPERATION operation, String keyName, Object key, String valueName, String value)
    {
        if(serverModel == null || serverModel.serverConnection == null || asClient.session == null) return null;
        if(tableName == null || tableName.isEmpty())  return null;

        try( Table table = ((Session)asClient.session).openTable(tableName, ((ASSessModel)serverModel).properties) ) {
        	return OPERATION.applyOperation(operation, table, keyName, key, valueName, value, ((ASSessModel)asClient).properties);

        }catch(DataGridException dataGridException) {
            dataGridException.printStackTrace(System.err);
            return null;
        }
    }

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		return StringUtil.jsonToObject(jsonParams, ASSessModel.class);
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!ASSessModel.class.isInstance(client)) return false;
		ASSessModel serverModel = (ASSessModel) client;

		if(StringUtil.isEmpty(client.serverUrl)) {
			serverModel.serverUrl = defaultServerUrl;
		}

		try {
			String gridName = serverModel.gridName;
			serverModel.properties = createDefaultProperties(serverModel);
			serverModel.serverConnection = DataGrid.connect(serverModel.serverUrl, gridName, serverModel.properties);
		} catch (DataGridException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void disconnectServer() {
		closeAllSession();
    	if(serverModel.serverConnection != null) {
    		try {
				((Connection)serverModel.serverConnection).close();
    		}catch(Exception e) {
    			e.printStackTrace();
    		}
			serverModel.serverConnection = null;
    	}
    	
    }
	
	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!ASSessModel.class.isInstance(client)) return null;
		ASSessModel asClient = (ASSessModel) client;
		return asClient.role;
	}

	@Override
	public String tokenizeSessionName(String prefixHandle) {
		// TODO Auto-generated method stub
		int lastidx = prefixHandle.lastIndexOf(defaultDelimiter);
		return prefixHandle.substring(lastidx + 1);
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!ASSessModel.class.isInstance(client)) return null;
		ASSessModel asClient = (ASSessModel) client;
		if(serverModel == null || serverModel.serverConnection == null || ((ASSessModel)serverModel).properties == null) return null;
		try {
			asClient.session = ((Connection)serverModel.serverConnection).createSession(((ASSessModel)serverModel).properties);
			return asClient;
		} catch (DataGridException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!ASSessModel.class.isInstance(client)) return;
		ASSessModel asClient = (ASSessModel) client;
		
		if(asClient.session != null) {
			try {
				((Session)asClient.session).close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			asClient.session = null;
		}
	}

	@Override
	public boolean create(String handle, String table, Pair<String,? extends Object> key, List<Pair<String,? extends Object>> params) {
		// TODO Auto-generated method stub

		Object client = sessionMap.get(handle);
		if(client != null && ASSessModel.class.isInstance(client)) {
			ASSessModel asSessModel = (ASSessModel) client;
			if(asSessModel.session != null) {
				try {
					return (Boolean)runCommand(asSessModel, table, OPERATION.PUT, key.getFirst(), key.getSecond(), params.get(0).getFirst(), (String)params.get(0).getSecond());
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public Object retrieve(String handle, String table, Pair<String,? extends Object> key) {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
		if(client != null && ASSessModel.class.isInstance(client)) {
			ASSessModel asSessModel = (ASSessModel) client;
			if(asSessModel.session != null) {
				try {
					return runCommand(asSessModel, table, OPERATION.GET, key.getFirst(), key.getSecond(), null, null);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public boolean update(String handle, String table, Pair<String,? extends Object> key, List<Pair<String,? extends Object>> params) {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
		if(client != null && ASSessModel.class.isInstance(client)) {
			ASSessModel asSessModel = (ASSessModel) client;
			if(asSessModel.session != null) {
				try {
					return (Boolean)runCommand(asSessModel, table, OPERATION.UPDATE, key.getFirst(), key.getSecond(), params.get(0).getFirst(), (String)params.get(0).getSecond());
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public boolean delete(String handle, String table, Pair<String,? extends Object> key) {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
		if(client != null && ASSessModel.class.isInstance(client)) {
			ASSessModel asSessModel = (ASSessModel) client;
			if(asSessModel.session != null) {
				try {
					return (Boolean)runCommand(asSessModel, table, OPERATION.DELETE, key.getFirst(), key.getSecond(), null, null);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}


    /**********************************************************************************************
     *
     * Beginning of methods to demonstrate ActiveSpaces "per-op" functionality.
     *
     * Each method illustrates how to perform a different operation using the ActiveSpaces APIs.
     *
     **********************************************************************************************/

    private boolean applyField(Row row, String name, Object value) {
    	try {
			if(Long.class.isInstance(value) || Integer.class.isInstance(value)) {
				row.setLong(name, (Long)value);
			}else if(String.class.isInstance(value)) {
				row.setString(name, (String)value);
			}else return false;
    	} catch(Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }

    /**
     * Insert a single row into the table.
     *
     * @param table the table to insert the row into
     * @param key the key for the row
     * @param value the value for the row
     */
    private Boolean putRow(Table table, String keyName, Object key, String valueName, Object value, Properties defaultProperties)
    {
        // create a row and set the user supplied key and value in it
        Row putRow = null;
        try
        {
            putRow = table.createRow();
            if(applyField(putRow, keyName, key) && applyField(putRow, valueName, value)){
            	table.put(putRow);
            	return true;
        	}
        }
        catch (DataGridException dataGridException)
        {
            dataGridException.printStackTrace(System.err);
        }
        finally
        {
            if (putRow != null)
            {
                try
                {
                    putRow.destroy();
                }
                catch (DataGridException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
        return false;
    }

    /**
     * Insert multiple rows into the table.
     *
     * Batches of rows are inserted into the table until the required number of rows have been inserted.
     *
     * @param session the session to use when doing the batch operation
     * @param table the table into which the rows are to be inserted
     * @param start the key of the first row
     * @param numRows the total number of rows to insert
     * @param batchSize the number of rows in each batch
     */
    private boolean putRows(Session session, Table table, String keyName, Map<Object, Pair<String, Object>> datas, Properties defaultProperties)
    {
    	if(datas.isEmpty()) return false;
    	
    	Row[] Rows = null;

    	try {
			Rows = datas.entrySet().stream()
				.filter(data -> CheckObject.test(data.getKey()))
				.map(data -> { 
					Row row = null;
					try {
						row = table.createRow();
						applyField(row, keyName, data.getKey());
						applyField(row, data.getValue().getFirst(), data.getValue().getSecond());
					} catch (DataGridException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return row;
				})
				.filter(row -> row != null)
				.toArray(Row[]::new);
			
			/** batch size 를 고려해야 하는지 체크 */
			try (BatchResult batch = session.putRows(Rows, defaultProperties))
			{
				if (batch.allSucceeded()) {
					return true;
				} else {
					System.out.printf("Batch failed%n");
					return false;
				}
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	} finally {
    		Arrays.stream(Rows).forEach(row -> {
    			try {
    				row.destroy();
    			}catch (Exception e) { }
    		});
        }
    }

	/**
     * Get a single row from the table. The value of the row is printed, if it exists.
     *
     * @param table the table to get the row from
     * @param key the key for the row
     */
    private String getRow(Table table, String keyName, Object key, Properties defaultProperties)
    {
        // create a row and set the user supplied key and value in it
        Row keyRow = null;
        String result = null;
        try
        {
            keyRow = table.createRow();
            applyField(keyRow, keyName, key);

            Row getRow = table.get(keyRow);
            if (getRow != null)
            {
            	/** check string structure */
            	result = getRow.toString();
                getRow.destroy();
                return result;
            }
            else
            {
            	return "";
            }
        }
        catch (DataGridException dataGridException)
        {
            dataGridException.printStackTrace(System.err);
            return "";
        }
        finally
        {
            if (keyRow != null)
            {
                try
                {
                    keyRow.destroy();
                }
                catch (DataGridException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Get multiple rows from the table.
     *
     * Batches of rows are requested from the table until the required number of rows have been retrieved.
     *
     * @param session the session to use when doing the batch operation
     * @param table the table from which the rows are to be requested
     * @param start the key of the first row
     * @param numRows the total number of rows to get
     * @param batchSize the number of rows in each batch
     */
    private String[] getRows(Session session, Table table, String keyName, Object[] keys, Properties defaultProperties)
    {
    	Row[] keyRows = null;
    	String[] result = null;
    	
    	try {
    		keyRows = Arrays.stream(keys)
				.filter(CheckObject)
				.map(key -> { 
					Row row = null;
					try {
						row = table.createRow();
						applyField(row, keyName, key);
					} catch (DataGridException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return row;
				})
				.filter(row -> row != null)
				.toArray(Row[]::new);
				
			try (BatchResult batch = session.getRows(keyRows, defaultProperties))
			{
				int size = batch.getSize();
				int index = 0;
				if(size > 0) {
					result = new String[size];
					for (int j = 0; j < size; j++)
					{
						Row getRow = batch.getRow(j);
						if (getRow != null)
						{
							result[index++] = getRow.toString();
						}
					}
				}
			}
			return result;
    	} catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
    	} finally {
    		Arrays.stream(keyRows).forEach(row -> {
    			try {
    				row.destroy();
    			}catch (Exception e) { }
    		});
    	}
    }

     /**
      * Update a single row in the table. It is not necessary to update every column in the row
      *
      * @param table the table to insert the row into
      * @param key the key for the row
      * @param value the value to update in the row
      */
     private Boolean updateRow(Table table, String keyName, Object key, String valueName, Object value, Properties defaultProperties)
     {
         // create a row and set the user supplied key and value in it
         Row updateRow = null;
         try
         {
             updateRow = table.createRow();
             if(applyField(updateRow, keyName, key) && applyField(updateRow, valueName, value)) {
				 table.update(updateRow);
				 return true;
             }
             return false;
         }
         catch (DataGridException dataGridException)
         {
             dataGridException.printStackTrace(System.err);
             return false;
         }
         finally
         {
             if (updateRow != null)
             {
                 try
                 {
                     updateRow.destroy();
                 }
                 catch (DataGridException e)
                 {
                     e.printStackTrace(System.err);
                 }
             }
         }
     }

    /**
     * Delete a single row from the table.
     *
     * @param table the table to delete the row from
     * @param key the key for the row
     */
    private Boolean deleteRow(Table table, String keyName, Object key, Properties defaultProperties)
    {
        // create a row and set the user supplied key and value in it
        Row keyRow = null;
        try
        {
            keyRow = table.createRow();
            if(applyField(keyRow, keyName, key)) {
				table.delete(keyRow);
				return true;
            }
            return false;
        }
        catch (DataGridException dataGridException)
        {
            dataGridException.printStackTrace(System.err);
            return false;
        }
        finally
        {
            if (keyRow != null)
            {
                try
                {
                    keyRow.destroy();
                }
                catch (DataGridException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Delete multiple rows from the table.
     *
     * Batches of rows are deleted from the table until the required number of rows have been deleted.
     *
     * @param session the session to use when doing the batch operation
     * @param table the table from which the rows are to be deleted
     * @param start the key of the first row
     * @param numRows the total number of rows to delete
     * @param batchSize the number of rows in each batch
     */
    private void deleteRows(Session session, Table table, String keyName, Object[] keys, Properties defaultProperties )
    {
        Row keyRows[] = null;
        try
        {
        	keyRows = Arrays.stream(keys).filter(CheckObject)
        		.map(key -> {
					Row row = null;
					try {
						row = table.createRow();
						applyField(row, keyName, key);
					} catch (DataGridException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return row;
        		})
        		.filter(row -> row != null)
        		.toArray(Row[]::new);

			try (BatchResult batch = session.deleteRows(keyRows, defaultProperties))
			{
				if (batch.allSucceeded()) { }
				else
				{
					System.out.printf("Batch failed%n");
				}
			}
        }
        catch (DataGridException dataGridException)
        {
            dataGridException.printStackTrace(System.err);
        }
        finally
        {
    		Arrays.stream(keyRows).forEach(row -> {
    			try {
    				row.destroy();
    			}catch (Exception e) { }
    		});
        }
    }

    /**
     * Iterate over the rows in the table.
     *
     * @param table the table to iterate over
     * @param filter if this is not null only rows that match this filter will be received
     */
    private void iterateTable(Table table, String filter, Properties defaultProperties)
    {
        System.out.printf("\tUsing filter: %s%n", filter);

        // Try-with-resources is being used, so a "finally" block isn't needed.
        try (RowSet rowSet = table.createRowSet(filter, defaultProperties))
        {
            long rowCount = 0;
            try
            {
                for (Row itrRow : rowSet)
                {
                    rowCount++;
                    try
                    {
                        System.out.printf("Row from iterator: %s%n", itrRow.toString());
                    }
                    finally
                    {
                        try
                        {
                            itrRow.destroy();
                        }
                        catch (DataGridException e)
                        {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
            catch (NoSuchElementException ex)
            {
                System.out.println("No such element exception received during iteration.");
                ex.printStackTrace(System.err);
            }

            System.out.printf("Iterated over %d rows in the table %n", rowCount);
        }
        catch (DataGridException e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Create a listener on the table and listen for events.
     *
     * @param table the table to listen to
     * @param filter if this is not null only events that match this filter will be received
     */
    private void listenToTableEvents(Table table, String filter, String eventTypes, Properties defaultProperties)
    {
        System.out.printf("\tUsing filter: %s%n", filter);

        class ListenerState
        {
            long count = 0;
            TableListener listener = null;
            ReentrantLock lock = new ReentrantLock();
            boolean closed = false;
            Properties props = null;
        }
        final ListenerState listenerState = new ListenerState();

        Properties props = new Properties(defaultProperties);
        if (eventTypes != null && !eventTypes.isEmpty())
        {
            props.setProperty(TableListener.TIBDG_LISTENER_PROPERTY_STRING_EVENT_TYPE_LIST, eventTypes);
        }
        listenerState.props = props;

        try
        {
            listenerState.listener = table.createTableListener(filter, new TableEventHandler()
            {
                @Override
                public void eventsReceived(List<Event> events, TableListener listener)
                {
                    try
                    {
                        for (Event event : events)
                        {
                            // Reference listenerState object defined outside the callback
                            listenerState.count++;

                            EventType eventType = event.getType();
                            System.out.printf("Received event %d of type %s on table %s:%n", listenerState.count, eventType, table.getName());
                            Row previous = null;
                            Row current = null;
                            boolean errorEvent = false;
                            switch (eventType)
                            {
                                case PUT:
                                    previous = event.getPrevious();
                                    current = event.getCurrent();
                                    break;
                                case DELETE:
                                case EXPIRED:
                                    previous = event.getPrevious();
                                    break;
                                case ERROR:
                                    errorEvent = true;
                                    break;
                                default:
                                    break;
                            }
                            if (previous != null)
                            {
                                System.out.printf("\tprevious: %s%n", previous.toString());
                            }
                            if (current != null)
                            {
                                System.out.printf("\tcurrent: %s%n", current.toString());
                            }
                            if (errorEvent)
                            {
                                System.out.printf("errorcode: %d description: %s%n", event.getErrorCode(),
                                        event.getErrorDescription());
                            }

                            if(eventType == EventType.ERROR)
                            {
                                int errorCode = event.getErrorCode();
                                if(errorCode == Event.TIBDG_EVENT_ERRORCODE_PROXY_FAILURE ||
                                        errorCode == Event.TIBDG_EVENT_ERRORCODE_COPYSET_LEADER_FAILURE ||
                                        errorCode == Event.TIBDG_EVENT_ERRORCODE_GRID_REDISTRIBUTING)
                                {
                                    listenerState.lock.lock();
                                    try
                                    {
                                        if(!listenerState.closed)
                                        {
                                            Table listenerTable = listener.getTable();
                                            String listenerFilter = listener.getFilter();
                                            listener.close();
                                            listener = null;
                                            listenerState.listener = null;
                                            System.out.println("Recreating listener after error...");

                                            try
                                            {
                                                listenerState.listener = listenerTable.createTableListener(listenerFilter, this, props);
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace(System.err);
                                            }

                                            int delay = 1;
                                            while (listenerState.listener == null)
                                            {
                                                System.out.println("Failed to recreate listener. Retrying again in " + delay + " seconds");
                                                Thread.sleep(delay * 1000);
                                                try
                                                {
                                                    listenerState.listener = listenerTable.createTableListener(listenerFilter, this, props);
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace(System.err);
                                                }
                                            }
                                            System.out.println("Listening on table...");
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace(System.err);
                                    }
                                    finally
                                    {
                                        listenerState.lock.unlock();
                                    }
                                }
                                else
                                {
                                    System.out.println("Listener now invalid.  Will not receive more events.");
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.err);
                    }
                }
            }, listenerState.props);

            System.out.println("Table Listener listening on table. Press [ENTER] to stop listening");
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                br.readLine();
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace(System.err);
            }
            System.out.println("Destroying Table Listener.");
        }
        catch (DataGridException e)
        {
            e.printStackTrace(System.err);
        }
        finally
        {

            listenerState.lock.lock();
            try
            {
                listenerState.closed = true;
                if(listenerState.listener != null)
                {
                    listenerState.listener.close();
                }
                listenerState.listener = null;
            }
            catch (DataGridException e)
            {
                e.printStackTrace(System.err);
            }
            finally
            {
                listenerState.lock.unlock();
            }
        }

    }

    /**
     * Create a SQL statement and execute it.
     *
     * @param session the session on which to create the statement
     * @param sqlString the SQL that will be executed
     */
    private void executeSqlStatement(Session session, String sqlString, Properties defaultProperties)
    {
        System.out.printf("Creating statement: %s%n", sqlString);

        // Statement extends Java 7 auto closeable
        // Try-with-resources is also supported to avoid needing the "finally" block.
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            statement = session.createStatement(sqlString, defaultProperties);
            ResultSetMetadata rsm = statement.getResultSetMetadata();
            if (rsm != null)
            {
                // process this as a SELECT statement
                resultSet = statement.executeQuery(defaultProperties);

                int columnCount = rsm.getColumnCount();
                System.out.printf("Result set contains %d columns:%n", columnCount);
                // column numbers start with 1
                for (int i = 1; i <= columnCount; i++)
                {
                    String columnName = rsm.getColumnName(i);
                    ColumnType columnType = rsm.getColumnType(i);
                    System.out.printf("\t%s\t%s%n", columnName, columnType.toString());
                }

                long rowCount = 0;
                try
                {
                    for (Row row : resultSet)
                    {
                        rowCount++;
                        try
                        {
                            System.out.printf("Row from result set: %s%n", row.toString());
                        }
                        finally
                        {
                            try
                            {
                                row.destroy();
                            }
                            catch (DataGridException e)
                            {
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                }
                catch (NoSuchElementException ex)
                {
                    System.out.println("No such element exception received during iteration.");
                    ex.printStackTrace(System.err);
                }
                System.out.printf("ResultSet contained %d rows.%n", rowCount);
            }
            else
            {
                long rowCount = statement.executeUpdate(defaultProperties);
                System.out.printf("Successfully executed statement. %d rows updated.%n", rowCount);
            }
        }
        catch (DataGridException e)
        {
            System.out.println("Failed to execute the statement.");
            e.printStackTrace(System.err);
        }
        catch (Exception ex)
        {
            // unspecified exception caught
            System.out.println("Failed to execute the statement.");
            ex.printStackTrace(System.err);
        }
        finally
        {
            if (resultSet != null)
            {
                try
                {
                    resultSet.close();
                }
                catch (Exception e)
                {
                    System.out.println("Failed to close ResultSet.");
                    e.printStackTrace(System.err);
                }
            }
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (Exception e)
                {
                    System.out.println("Failed to close Statement.");
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Execute a SQL DDL statement.
     *
     * @param session the session to use when executing the statement
     * @param sqlString the SQL that will be executed
     */
    private void executeSqlUpdate(Session session, String sqlString, Properties defaultProperties)
    {
        try
        {
            if (sqlString != null && !sqlString.equals(""))
            {
                System.out.printf("Executing update: %s%n", sqlString);
            }
            else
            {
                sqlString = null;
            }

            long res = session.executeUpdate(sqlString, defaultProperties);
            System.out.printf("Result of executing the update: %d%n", res);
        }
        catch (DataGridException e)
        {
            System.out.println("Failed to execute the update.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Get the metadata for either the whole data grid or just the table, and print it.
     *
     * @param connection the connection to use when retrieving the metadata
     * @param mdType the type of metadata to print
     * @param tableName the name of the table
     * @param checkpointName the name of the checkpoint, if set
     */
    private void getMetadata(Connection connection, MetadataType mdType, String tableName, String checkpointName, Properties defaultProperties)
    {
        Properties props = defaultProperties;
        if (checkpointName != null)
        {
            props = new Properties(defaultProperties);
            props.setProperty(GridMetadata.TIBDG_GRIDMETADATA_PROPERTY_STRING_CHECKPOINT_NAME, checkpointName);
        }

        // Try-with-resources is being used, so a "finally" block isn't needed.
        try (GridMetadata gridMetadata = connection.getGridMetadata(props))
        {
            switch (mdType)
            {
                case GRID:
                    printGridMetadata(gridMetadata);
                    break;
                case TABLE:
                    TableMetadata tableMetadata = gridMetadata.getTableMetadata(tableName);
                    if (tableMetadata == null)
                    {
                        System.err.printf("Table '%s' not found%n", tableName);
                    }
                    else
                    {
                        printTableMetadata(tableMetadata);
                    }
                    break;
                default:
                    System.out.println("Invalid metadata display option entered");
                    break;
            }
        }
        catch (DataGridException dataGridException)
        {
            dataGridException.printStackTrace(System.err);
        }
    }

    /**
     * Print the metadata for the specific table.
     *
     * @param tableMetadata the metadata for the table
     * @throws DataGridException if there was an error accessing the table's metadata
     */
    private void printTableMetadata(TableMetadata tableMetadata) throws DataGridException
    {
        System.out.printf("Table '%s':%n", tableMetadata.getName());
        System.out.printf("\tName = %s%n", tableMetadata.getName());
        if (tableMetadata.getDefaultTTL() != 0)
            System.out.printf("\tExpiration enabled, default TTL = %d%n", tableMetadata.getDefaultTTL());
        System.out.printf("\tPrimary Index = %s%n", tableMetadata.getPrimaryIndexName());

        String[] columnNames = tableMetadata.getColumnNames();

        System.out.printf("\t%d columns%n", columnNames.length);
        for (String columnName : columnNames)
        {
            System.out.printf("\t\t%s (%s)%n", columnName, tableMetadata.getColumnType(columnName));
        }

        String[] indexNames = tableMetadata.getIndexNames();

        System.out.printf("\t%d indexes%n", indexNames.length);
        for (String indexName : indexNames)
        {
            System.out.printf("\t\t%s (", indexName);

            String[] indexColumnNames = tableMetadata.getIndexColumnNames(indexName);
            System.out.printf(String.join(", ", indexColumnNames));
            System.out.printf(")%n");
        }
        System.out.printf("%n");
    }

    /**
     * Print the metadata for the whole data grid.
     *
     * @param gridMetadata the metadata for the whole data grid
     * @throws DataGridException if there was an error accessing the data grid's metadata
     */
    private void printGridMetadata(GridMetadata gridMetadata) throws DataGridException
    {
        System.out.printf("AS Product Version: '%s'%nGrid Name: '%s'%n",
                gridMetadata.getVersion(), gridMetadata.getGridName());

        String[] tableNames = gridMetadata.getTableNames();

        System.out.printf("The data grid contains %d table(s):%n", tableNames.length);

        for (String tableName : tableNames)
        {
            TableMetadata tableMetadata = gridMetadata.getTableMetadata(tableName);
            printTableMetadata(tableMetadata);
        }
    }

    /**********************************************************************************************
     *
     * End of methods to demonstrate ActiveSpaces "per-op" functionality.
     *
     **********************************************************************************************/

    /**
     * Create a default Properties object to be used for all operations.
     *
     * The Properties object controls aspects of the Connection and Session such as how long to wait for a connection to
     * be established, any username, password and trust information, and whether the session is transacted or not.
     * 
     * Other properties may be inherited from System properties.
     *
     * @return a Properties object
     */
    private Properties createDefaultProperties(ASSessModel asClient)
    {
        Properties props = new Properties(System.getProperties());
        props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_CLIENT_LABEL, asClient.label);
        props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_DOUBLE_TIMEOUT, String.valueOf(asClient.connectionTimeout));
        props.setProperty(Session.TIBDG_SESSION_PROPERTY_BOOLEAN_TRANSACTED, String.valueOf(asClient.doTxn));

        if (asClient.username != null && asClient.password != null)
        {
            props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_USERNAME, asClient.username);
            props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_USERPASSWORD, asClient.password);
        }

        boolean secureRealm = asClient.serverUrl.toLowerCase().startsWith("https://");

        if (secureRealm)
        {
            if (asClient.trustAll)
            {
                props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_TRUST_TYPE,
                        Connection.TIBDG_CONNECTION_HTTPS_CONNECTION_TRUST_EVERYONE);
            }
            else if (asClient.trustFileName != null)
            {
                props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_TRUST_TYPE,
                        Connection.TIBDG_CONNECTION_HTTPS_CONNECTION_USE_SPECIFIED_TRUST_FILE);
                props.setProperty(Connection.TIBDG_CONNECTION_PROPERTY_STRING_TRUST_FILE,
                        asClient.trustFileName);
            }
        }

        if (asClient.checkpointName != null)
        {
            props.setProperty(Session.TIBDG_SESSION_PROPERTY_STRING_CHECKPOINT_NAME,
                    asClient.checkpointName);
        }

        return props;
    }


    /**
     * Checks that a table with the given name exists in the data grid and that it has the appropriate columns
     *
     * @param gridMetadata the metadata for the whole data grid
     * @param tableName the name of the table
     * @throws DataGridException if there was an error accessing the table and its schema
     */
    private void validate(GridMetadata gridMetadata, String tableName) throws DataGridException
    {
        // Display the product version info before validating
        System.out.printf("AS Product Version: %s%n", gridMetadata.getVersion());

        // check that the table to be used exists
        TableMetadata tableMetadata = gridMetadata.getTableMetadata(tableName);
        if (tableMetadata == null)
        {
            System.err.printf("No table '%s' configured in the data grid%n", tableName);
            throw new RuntimeException("Table not found");
        }

        // check that the user specified table has the appropriate columns
        // this example only supports tables with a primary key column
        // of type long and named 'key' and a string column named 'value'
        ColumnType keyType = tableMetadata.getColumnType("key");
        if (keyType != ColumnType.LONG)
        {
            System.err.printf("Column 'key' is not of type Long");
            throw new RuntimeException("Column 'key' is not of type Long");
        }

        ColumnType valueType = tableMetadata.getColumnType("value");
        if (valueType != ColumnType.STRING)
        {
            System.err.printf("Column 'value' is not of type String");
            throw new RuntimeException("Column 'value' is not of type String");
        }
    }



    /** need algorithm */
    public static void main(String[] args)
    {
        System.out.println("ActiveSpaces Example: Operations");
        try
        {
//            Operations.initialize(args);
//            Operations.runCommand("t1", "p", 2, "200");
//            Operations.runCommand("t1", "p", 3, "300");
//            Operations.runCommand("t1", "p", 4, "400");
//            Operations.runCommand("t1", "p", 5, "500");
//
//            System.out.println("Get(" + 2 + "):" + Operations.runCommand("t1", "g", 2, null) );
//            System.out.println("Get(" + 3 + "):" + Operations.runCommand("t1", "g", 3, null) );
//            System.out.println("Get(" + 4 + "):" + Operations.runCommand("t1", "g", 4, null) );
//            System.out.println("Get(" + 5 + "):" + Operations.runCommand("t1", "g", 5, null) );
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

}

