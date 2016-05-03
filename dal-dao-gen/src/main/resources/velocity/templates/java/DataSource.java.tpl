<Datasources>
#foreach($resource in $host.getResources())	
	<Datasource name="${resource.getName()}"
              userName="root"
              password="123456"
              connectionUrl="jdbc:mysql://127.0.0.1:3306/${resource.getName()}"
              driverClassName="com.mysql.jdbc.Driver"
              testWhileIdle="false"
              testOnBorrow="false"
              testOnReturn="false"
              validationQuery="SELECT 1"
              validationInterval="30000"
              timeBetweenEvictionRunsMillis="30000"
              maxActive="100"
              minIdle="1"
              maxWait="10000"
              initialSize="1"
              removeAbandonedTimeout="60"
              removeAbandoned="true"
              logAbandoned="true"
              minEvictableIdleTimeMillis="30000"
              option="sendStringParametersAsUnicode=false"/>
#end			  
</Datasources>
