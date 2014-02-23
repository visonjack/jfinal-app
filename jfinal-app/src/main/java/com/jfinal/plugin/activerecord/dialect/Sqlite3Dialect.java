/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.dialect;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jfinal.sog.kit.StringPool;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.TableInfo;

/**
 * SqliteDialect.
 */
public class Sqlite3Dialect extends Dialect {
	
	public String forTableInfoBuilderDoBuildTableInfo(String tableName) {
		return "SELECT * FROM " + tableName + " WHERE 1 = 2";
	}
	
	public void forModelSave(TableInfo tableInfo, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("INSERT INTO ").append(tableInfo.getTableName()).append(StringPool.LEFT_BRACKET);
		StringBuilder temp = new StringBuilder(") VALUES(");
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (tableInfo.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append(colName);
				temp.append(StringPool.QUESTION_MARK);
				paras.add(e.getValue());
			}
		}
		sql.append(temp.toString()).append(StringPool.RIGHT_BRACKET);
	}
	
	public String forModelDeleteById(TableInfo tInfo) {
		String pKey = tInfo.getPrimaryKey();
		StringBuilder sql = new StringBuilder(45);
		sql.append("delete from ");
		sql.append(tInfo.getTableName());
		sql.append(" where ").append(pKey).append(" = ?");
		return sql.toString();
	}
	
	public void forModelUpdate(TableInfo tableInfo, Map<String, Object> attrs, Set<String> modifyFlag, String pKey, Object id, StringBuilder sql, List<Object> paras) {
		sql.append(SQL_UPDATE).append(tableInfo.getTableName()).append(" SET ");
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (!pKey.equalsIgnoreCase(colName) && modifyFlag.contains(colName) && tableInfo.hasColumnLabel(colName)) {
				if (paras.size() > 0)
					sql.append(", ");
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(SQL_WHERE).append(pKey).append(" = ?");
		paras.add(id);
	}
	
	public String forModelFindById(TableInfo tInfo, String columns) {
		StringBuilder sql = new StringBuilder("SELECT ");
		if (columns.trim().equals(StringPool.ASTERISK)) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(StringPool.COMMA);
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(SQL_FROM);
		sql.append(tInfo.getTableName());
		sql.append(SQL_WHERE).append(tInfo.getPrimaryKey()).append(" = ?");
		return sql.toString();
	}
	
	public String forDbFindById(String tableName, String primaryKey, String columns) {
		StringBuilder sql = new StringBuilder("SELECT ");
		if (columns.trim().equals(StringPool.ASTERISK)) {
			sql.append(columns);
		}
		else {
			String[] columnsArray = columns.split(StringPool.COMMA);
			for (int i=0; i<columnsArray.length; i++) {
				if (i > 0)
					sql.append(", ");
				sql.append(columnsArray[i].trim());
			}
		}
		sql.append(SQL_FROM);
		sql.append(tableName.trim());
		sql.append(SQL_WHERE).append(primaryKey).append(" = ?");
		return sql.toString();
	}
	
	public String forDbDeleteById(String tableName, String primaryKey) {
        return "DELETE FROM " + tableName.trim() + SQL_WHERE + primaryKey + " = ?";
	}
	
	public void forDbSave(StringBuilder sql, List<Object> paras, String tableName, Record record) {
		sql.append("INSERT INTO ");
		sql.append(tableName.trim()).append(StringPool.LEFT_BRACKET);
		StringBuilder temp = new StringBuilder();
		temp.append(") VALUES(");
		
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append(e.getKey());
			temp.append(StringPool.QUESTION_MARK);
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(StringPool.RIGHT_BRACKET);
	}
	
	public void forDbUpdate(String tableName, String primaryKey, Object id, Record record, StringBuilder sql, List<Object> paras) {
		sql.append(SQL_UPDATE).append(tableName.trim()).append(" set ");
		for (Entry<String, Object> e: record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!primaryKey.equalsIgnoreCase(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(SQL_WHERE).append(primaryKey).append(" = ?");
		paras.add(id);
	}
	
	public void forPaginate(StringBuilder sql, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
		int offset = pageSize * (pageNumber - 1);
		sql.append(select).append(StringPool.SPACE);
		sql.append(sqlExceptSelect);
		sql.append(" limit ").append(offset).append(", ").append(pageSize);
	}
}
