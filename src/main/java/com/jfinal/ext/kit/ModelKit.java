/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2014 sagyf Yang. The Four Group.
 */
package com.jfinal.ext.kit;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.TableInfo;
import com.jfinal.plugin.activerecord.TableInfoMapping;

public class ModelKit {

    protected final static Logger logger = Logger.getLogger(ModelKit.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Record toRecord(Model model) {
        Record record = new Record();
        Set<Entry<String, Object>> attrs = model.getAttrsEntrySet();
        for (Entry<String, Object> entry : attrs) {
            record.set(entry.getKey(), entry.getValue());
        }
        return record;
    }

    @SuppressWarnings("rawtypes")
    public static Model set(Model model, Object... attrsAndValues) {
        int length = attrsAndValues.length;
        Preconditions.checkArgument(length % 2 == 0, "attrsAndValues length must be even number", length);
        for (int i = 0; i < length; i = i + 2) {
            Object attr = attrsAndValues[i];
            Preconditions.checkArgument(attr instanceof String, "the odd number of attrsAndValues  must be String");
            model.set((String) attr, attrsAndValues[i + 1]);
        }
        return model;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map<String, Object> toMap(Model model) {
        Map<String, Object> map = Maps.newHashMap();
        Set<Entry<String, Object>> attrs = model.getAttrsEntrySet();
        for (Entry<String, Object> entry : attrs) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static Model<?> fromBean(Class<? extends Model<?>> clazz, Object bean) {
        Model<?> model = null;
        try {
            model = clazz.newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return model;
        }
        //TODO bean to model
        return model;
    }

    @SuppressWarnings("rawtypes")
    public static int[] batchSave(List<? extends Model> data) {
        return batchSave(data, data.size());
    }

    @SuppressWarnings("rawtypes")
    public static int[] batchSave(List<? extends Model> data, int batchSize) {
        Model model = data.get(0);
        Map<String, Object> attrs = Reflect.on(model).field("attrs").get();
        Class<? extends Model> modelClass = model.getClass();
        TableInfo tableInfo = TableInfoMapping.me().getTableInfo(modelClass);
        StringBuilder sql = new StringBuilder();
        List<Object> paras = Lists.newArrayList();
        DbKit.getDialect().forModelSave(tableInfo, attrs, sql, paras);
        Object[][] batchPara = new Object[data.size()][attrs.size()];
        for (int i = 0; i < data.size(); i++) {
            int j = 0;
            for (String key : attrs.keySet()) {
                batchPara[i][j++] = data.get(i).get(key);
            }
        }
        return Db.batch(sql.toString(), batchPara, batchSize);
    }

    public static int hashCode(Model<?> model) {
        final int prime = 31;
        int result = 1;
        TableInfo tableinfo = TableInfoMapping.me().getTableInfo(model.getClass());
        Set<Entry<String, Object>> attrsEntrySet = model.getAttrsEntrySet();
        for (Entry<String, Object> entry : attrsEntrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class<?> clazz = tableinfo.getColType(key);
            if (clazz == Integer.class) {
                result = prime * result + (Integer) value;
            } else if (clazz == Short.class) {
                result = prime * result + (Short) value;
            } else if (clazz == Long.class) {
                result = prime * result + (int) ((Long) value ^ ((Long) value >>> 32));
            } else if (clazz == Float.class) {
                result = prime * result + Float.floatToIntBits((Float) value);
            } else if (clazz == Double.class) {
                long temp = Double.doubleToLongBits((Double) value);
                result = prime * result + (int) (temp ^ (temp >>> 32));
            } else if (clazz == Boolean.class) {
                result = prime * result + ((Boolean) value ? 1231 : 1237);
            } else if (clazz == Model.class) {
                result = hashCode((Model<?>) value);
            } else {
                result = prime * result + ((value == null) ? 0 : value.hashCode());
            }
        }
        return result;
    }

    public static boolean equals(Model<?> model, Object obj) {
        if (model == obj)
            return true;
        if (obj == null)
            return false;
        if (model.getClass() != obj.getClass())
            return false;
        Model<?> other = (Model<?>) obj;
        TableInfo tableinfo = TableInfoMapping.me().getTableInfo(model.getClass());
        Set<Entry<String, Object>> attrsEntrySet = model.getAttrsEntrySet();
        for (Entry<String, Object> entry : attrsEntrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class<?> clazz = tableinfo.getColType(key);
            if (clazz == Float.class) {
            } else if (clazz == Double.class) {
            } else if (clazz == Model.class) {
            } else {
                if (value == null) {
                    if (other.get(key) != null)
                        return false;
                } else if (!value.equals(other.get(key)))
                    return false;
            }
        }
        return true;
    }
}
