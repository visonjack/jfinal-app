/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2014 sagyf Yang. The Four Group.
 */

package japp.db.dialect;

import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import japp.JApp;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * PostgreSqlDialect.
 */
public class PostgreSqlDialect extends Dialect {
	
	public String forTableBuilderDoBuild(String tableName) {
		return "select * from \"" + tableName + "\" where 1 = 2";
	}
	
	public void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("insert into \"").append(table.getName()).append("\"(");
		StringBuilder temp = new StringBuilder(") values(");
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append("\"").append(colName).append("\"");
				temp.append("?");
				paras.add(e.getValue());
			}
		}
		sql.append(temp.toString()).append(")");
	}
	
	public String forModelDeleteById(Table table) {
		String primaryKey = table.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from \"");
		sql.append(table.getName());
		sql.append("\" where \"").append(primaryKey).append("\" = ?");
		return sql.toString();
	}
	
	public void forModelUpdate(Table table, Map<String, Object> attrs, Set<String> modifyFlag, String primaryKey, Object id, StringBuilder sql, List<Object> paras) {
		sql.append("update \"").append(table.getName()).append("\" set ");
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (!primaryKey.equalsIgnoreCase(colName) && modifyFlag.contains(colName) && table.hasColumnLabel(colName)) {
				if (paras.size() > 0)
					sql.append(", ");
				sql.append("\"").append(colName).append("\" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where \"").append(primaryKey).append("\" = ?");
		paras.add(id);
	}
	
	public String forModelFindById(Table table, String columns) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
            sql.append(JApp.mode.isDev() ? columns : table.getColumnSelectSql());
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append("\"").append(columnsArray[i].trim()).append("\"");
			}
		}
		sql.append(" from \"");
		sql.append(table.getName());
		sql.append("\" where \"").append(table.getPrimaryKey()).append("\" = ?");
		return sql.toString();
	}
	
	public String forDbFindById(String tableName, String primaryKey, String columns) {
		StringBuilder sql = new StringBuilder("select ");
		if (columns.trim().equals("*")) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(",");
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append("\"").append(columnsArray[i].trim()).append("\"");
			}
		}
		sql.append(" from \"");
		sql.append(tableName.trim());
		sql.append("\" where \"").append(primaryKey).append("\" = ?");
		return sql.toString();
	}
	
	public String forDbDeleteById(String tableName, String primaryKey) {
		StringBuilder sql = new StringBuilder("delete from \"");
		sql.append(tableName.trim());
		sql.append("\" where \"").append(primaryKey).append("\" = ?");
		return sql.toString();
	}
	
	public void forDbSave(StringBuilder sql, List<Object> paras, String tableName, Record record) {
		sql.append("insert into \"");
		sql.append(tableName.trim()).append("\"(");
		StringBuilder temp = new StringBuilder();
		temp.append(") values(");
		
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append("\"").append(e.getKey()).append("\"");
			temp.append("?");
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(")");
	}
	
	public void forDbUpdate(String tableName, String primaryKey, Object id, Record record, StringBuilder sql, List<Object> paras) {
		sql.append("update \"").append(tableName.trim()).append("\" set ");
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!primaryKey.equalsIgnoreCase(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append("\"").append(colName).append("\" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where \"").append(primaryKey).append("\" = ?");
		paras.add(id);
	}
	
	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int offset = pageSize * (pageNumber - 1);
		sql.append(select).append(" ");
		sql.append(sqlExceptSelect);
		sql.append(" limit ").append(pageSize).append(" offset ").append(offset);
	}
}
