package davinDBMS.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.Column;
import davinDBMS.entity.Table;

public abstract class DDLQuery extends Query{ //has methods for DDL queries


}
