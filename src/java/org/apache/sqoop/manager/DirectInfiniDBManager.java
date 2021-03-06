package org.apache.sqoop.manager;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.util.ExportException;
import org.apache.sqoop.mapreduce.InfiniDBExportJob;
import org.apache.sqoop.mapreduce.db.DBConfiguration;

public class DirectInfiniDBManager extends DirectMySQLManager {

	public static final Log LOG = LogFactory.getLog(
			DirectInfiniDBManager.class.getName());

	public DirectInfiniDBManager(SqoopOptions options) {
		super(options);
	}

  
	/**
	 * Export the table from HDFS by using cpimport to insert the data
	 * back into the database.
	 */
	@Override
	public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
		throws IOException, ExportException {
		context.setConnManager(this);
		
		if (context.getOptions().getColumns() != null) {
		    LOG.warn("Direct-mode export from InfiniDB does not support column");
		    LOG.warn("selection. Falling back to JDBC-based import.");
		    // Don't warn them "This could go faster..."
		    MySQLManager.markWarningPrinted();
		    // Use JDBC-based importTable() method.
		    // todo have to figure out how to call the grandparent method here
		    super.exportTable(context);
		    return;
		}
		
		// If InfiniDB isn't installed in the default location, and there's no command line
		// over-ride (deprecated), there should be an environment variable pointing to the location.
		if (context.getOptions().getConf().get(DBConfiguration.INFINIDB_BIN_PATH) == null) {
			String InfiniDBBin = System.getenv("INFINIDB_INSTALL_DIR");
			if (InfiniDBBin != null && !InfiniDBBin.isEmpty()) {
				InfiniDBBin = InfiniDBBin + "/bin";
				context.getOptions().getConf().set(DBConfiguration.INFINIDB_BIN_PATH, InfiniDBBin);
			}
		}
		InfiniDBExportJob exportJob = new InfiniDBExportJob(context);
		exportJob.runExport();
	}	  
}
