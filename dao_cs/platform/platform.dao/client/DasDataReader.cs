﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using platform.dao.param;
using platform.dao.exception;
using platform.dao.utils;

namespace platform.dao.client
{
    public class DasDataReader : IDataReader
    {
        private static readonly DateTime utcStartTime;

        static DasDataReader()
        {
            utcStartTime = new DateTime(1970, 1, 1, 0, 0, 0, 0);
        }

        private int cursor = 0;

        private int rowCursor = 0;

        private List<IParameter> current;

        public List<List<IParameter>> ResultSet { get; set; }

        public void Close()
        {
            this.Dispose();
        }

        public int Depth
        {
            get { return 1; }
        }

        public DataTable GetSchemaTable()
        {
            throw new NotImplementedException();
        }

        public bool IsClosed
        {
            get { return null == current; }
        }

        public bool NextResult()
        {
            return Read();
        }

        public bool Read()
        {
            var result = cursor < ResultSet.Count;
            if (result)
            {
                current = ResultSet[cursor].OrderBy(o => o.Index).ToList();
                cursor++;
            }
            return result;
        }

        public int RecordsAffected
        {
            get { throw new NotImplementedException(); }
        }

        public void Dispose()
        {
            this.current.Clear();
            this.ResultSet.Clear();
            this.current = null;
            this.ResultSet = null;
        }

        /// <summary>
        /// 获取当前行中的列数
        /// </summary>
        public int FieldCount
        {
            get { return current.Count; }
        }

        public bool GetBoolean(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsBoolean();
        }

        public byte GetByte(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsByte();
        }

        public long GetBytes(int i, long fieldOffset, byte[] buffer, int bufferoffset, int length)
        {
            throw new NotImplementedException();
        }

        public char GetChar(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return (char)current[i].Value.AsUInt16();
        }

        public long GetChars(int i, long fieldoffset, char[] buffer, int bufferoffset, int length)
        {
            throw new NotImplementedException();
        }

        public IDataReader GetData(int i)
        {
            throw new NotImplementedException();
        }

        public string GetDataTypeName(int i)
        {
            throw new NotImplementedException();
        }

        public DateTime GetDateTime(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return utcStartTime.AddMilliseconds(current[i].Value.AsUInt64());
        }

        public decimal GetDecimal(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return decimal.Parse(current[i].Value.AsString());
        }

        public double GetDouble(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsDouble();
        }

        public Type GetFieldType(int i)
        {
            return TypeConverter.ResolveDbType(current[i].DbType);
        }

        public float GetFloat(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsSingle();
        }

        public Guid GetGuid(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return new Guid(current[i].Value.AsBinary());
        }

        public short GetInt16(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsInt16();
        }

        public int GetInt32(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsInt32();
        }

        public long GetInt64(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsInt64();
        }

        public string GetName(int i)
        {
            foreach (var p in current)
            {
                if (p.Index.Equals(i))
                {
                    return p.Name;
                }
            }
            return null;
        }

        public int GetOrdinal(string name)
        {
            foreach (var p in current)
            {
                if (p.Name.Equals(name))
                {
                    return p.Index;
                }
            }
            return -1;
        }

        public string GetString(int i)
        {
            if (i > FieldCount)
                throw new DAOException("Index out of bound!");

            return current[i].Value.AsString();
        }

        public object GetValue(int i)
        {
            return current[i].Value.ToObject();
        }

        public int GetValues(object[] values)
        {
            throw new NotImplementedException();
        }

        public bool IsDBNull(int i)
        {
            return current[i].Value.IsNil;
        }

        public object this[string name]
        {
            get
            {
                object result = null;
                IParameter param = null;
                foreach (var p in current)
                {
                    if (p.Name.Equals(name))
                    {
                        param = p;
                        break;
                    }
                }

                if (param != null)
                {
                    if (param.DbType == DbType.Decimal)
                        result = decimal.Parse(param.Value.AsString());
                    else if (param.DbType == DbType.StringFixedLength)
                        result = (char)param.Value.AsUInt16();
                    else if (param.DbType == DbType.Guid)
                        result = new Guid(param.Value.AsBinary());
                    else if (param.DbType == DbType.DateTime)
                        result = utcStartTime.AddMilliseconds(param.Value.AsUInt64());
                    else
                        result = param.Value.ToObject();
                }
                
                return result;
            }
        }

        public object this[int i]
        {
            get
            {
                object result = null;
                IParameter param = current[i];

                if (param != null)
                {
                    if (param.DbType == DbType.Decimal)
                        result = decimal.Parse(param.Value.AsString());
                    else if (param.DbType == DbType.StringFixedLength)
                        result = (char)param.Value.AsUInt16();
                    else if (param.DbType == DbType.Guid)
                        result = new Guid(param.Value.AsBinary());
                    else if (param.DbType == DbType.DateTime)
                        result = utcStartTime.AddMilliseconds(param.Value.AsUInt64());
                    else
                        result = param.Value.ToObject();
                }

                return result;
            }
        }
    }
}
