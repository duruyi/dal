#if($host.isTable())      
#if($host.generateAPI(46,53) && !$host.isSpa())
#set($returnType=[])
#foreach($p in $host.getColumns())
	#if($p.isIdentity() || $p.isPrimary())
		#set($success = $returnType.add(${p.getType()}))
	#end
#end
#if($returnType.size()<1)
	#set($success = $returnType.add("int"))
#end
        /// <summary>
        ///  插入${host.getClassName()}
        /// </summary>
        /// <param name="${WordUtils.uncapitalize(${host.getClassName()})}">${host.getClassName()}实体对象</param>
        /// <returns>新增的主键,如果有多个主键则返回第一个主键</returns>
		public $returnType.get(0) Insert${host.getClassName()}(${host.getClassName()} ${WordUtils.uncapitalize($host.getClassName())})
        {
            try
            {
                Object result = baseDao.Insert<${host.getClassName()}>(${WordUtils.uncapitalize($host.getClassName())});
##int iReturn = Convert.ToInt32(result);
#foreach($s in $returnType)
#set($index = $velocityCount - 1 )
#if($index==0)
		        iReturn = ($returnType.get($index))result;
#else	
			    //iReturn = ($returnType.get($index))result;
#end	
#end
                return iReturn;
            }
            catch (Exception ex)
            {
                throw new DalException("调用${host.getClassName()}时，访问Insert时出错", ex);
            }
        }
#end
#end
