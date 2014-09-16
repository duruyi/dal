#foreach($method in $host.getMethods())
#if($method.getCrud_type() == "select")
##实体类型并且返回值为List
#if($method.isReturnList() && !$method.isSampleType())
	/**
	 * ${method.getComments()}
	**/
	public List<${host.getPojoClassName()}> ${method.getName()}(${method.getParameterDeclaration()}) throws SQLException {
#if($method.isPaging())
		String sqlPattern = "${method.getPagingSql($host.getDatabaseCategory())}";
		String sql = String.format(sqlPattern, (pageNo - 1) * pageSize + 1, pageSize * pageNo);
#else
		String sql = "${method.getSql()}";
#end
#if($method.isInClauses())
		sql = SQLParser.parse(sql, ${method.getInClauses()});
#end
		StatementParameters parameters = new StatementParameters();
		hints = DalHints.createIfAbsent(hints);
#if($method.hasParameters())
		int i = 1;
#foreach($p in $method.getParameters())
#if($p.isInParameter())
		i = parameters.setInParameter(i, ${p.getJavaTypeDisplay()}, ${p.getAlias()});
#else
		parameters.set(i++, ${p.getJavaTypeDisplay()}, ${p.getAlias()});
#end
#end
#end
		return queryDao.query(sql, parameters, hints, parser);
	}
#end
#end
#end