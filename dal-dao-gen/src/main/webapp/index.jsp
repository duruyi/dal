<%@page pageEncoding="UTF-8"%>
<%@ page import="org.jasig.cas.client.util.AssertionHolder" %>
<!DOCTYPE html>
<html lang="en">
   <head>
      <!-- Meta, title, CSS, favicons, etc. -->
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <meta name="description" content="">
      <meta name="author" content="">
      <title>Ctrip DAO Generator</title>
      <!-- Bootstrap core CSS -->
      <link href="/static/bootstrap/css/bootstrap.min.css" rel="stylesheet">
      <link href="/static/bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
      <link href="/static/w2ui/w2ui-1.3.min.css" rel="stylesheet"/>
      <link href="/static/font-awesome/css/font-awesome.css" rel="stylesheet">
      <link href="/static/css/common.css" rel="stylesheet">
      <style type="text/css">
         body {
         /*padding-top: 32px;*/
         }
      </style>
      <!-- Documentation extras -->
      <!-- 
         <link href="../css/docs.css" rel="stylesheet">
         -->
      <!-- 
         <link href="../css/pygments-manni.css" rel="stylesheet">
         -->
      <!--[if lt IE 9]>
      <script src="./docs-assets/js/ie8-responsive-file-warning.js"></script>
      <![endif]-->
      <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
      <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
      <![endif]-->
      <!-- Favicons -->
      <link rel="shortcut icon" href="/static/images/favicon.ico">
   </head>
   <body>
      <!-- Docs master nav -->
      <div class="dal-navbar navbar navbar-inverse navbar-fixed-top" role="banner">
         <div class="navbar-header">
            <a href="/">
            <img class="logo" src="/static/images/logo.png" style="padding:5px;float:left;">
            </a>
         </div>
         <div class="collapse navbar-collapse in dal-navbar-collapse" role="navigation">
            <ul class="nav navbar-nav">
               <li class="active dropdown">
                  <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
                  <span class="dao_gen">
                  DAO Generator
                  </span>
                  <i class="fa fa-angle-down">
                  </i>
                  </a>
                  <ul class="dropdown-menu">
                     <li>
                        <a href="index.jsp">
                        <i class="fa fa-tasks">
                        </i>
                        DAO
                        </a>
                     </li>
                     <li>
                        <a href="file.jsp">
                        <i class="fa fa-eye">
                        </i>
                        Preview
                        </a>
                     </li>
                  </ul>
               </li>
               <li>
                  <a href="http://localhost:8080">
                  DAS Console
                  </a>
               </li>
            </ul>
            <ul class="nav navbar-nav pull-right">
               <li class="dropdown user">
                  <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
                  <span class="username">
                  <%=AssertionHolder.getAssertion().getPrincipal().getAttributes().get("sn")%>
                  </span>
                  <i class="fa fa-angle-down">
                  </i>
                  </a>
                  <ul class="dropdown-menu">
                     <li>
                        <a href="/logout.jsp">
                        <i class="fa fa-power-off">
                        </i>
                        Log Out
                        </a>
                     </li>
                  </ul>
               </li>
            </ul>
         </div>
      </div>
      <div id="main_layout">
      </div>
      <div class="modal fade" id="projectModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" is_update="0">
         <div class="modal-dialog">
            <div class="modal-content">
               <div class="modal-header">
                  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                  <h4 class="modal-title" id="myModalLabel">Add a project</h4>
               </div>
               <div class="modal-body">
                  <div class="row-fluid">
                     <div class="control-group">
                        <input id="project_id" type="hidden" value="">
                        <label class="control-label popup_label">项目名称</label>
                        <input id="name" class="span9" type="text">
                     </div>
                  </div>
                  <div class="row-fluid">
                     <div class="control-group">
                        <label class="control-label popup_label">命名空间</label>
                        <input id="namespace" class="span9" type="text">
                     </div>
                  </div>
               </div>
               <div class="modal-footer">
                  <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                  <button id="save_proj" type="button" class="btn btn-primary">Save changes</button>
               </div>
            </div>
            <!-- /.modal-content -->
         </div>
         <!-- /.modal-dialog -->
      </div>
      <!-- /.modal -->
      <!--Begin wizard-->
      <div class="modal fade" id="page1" tabindex="-1" role="dialog" aria-labelledby="page1_label" aria-hidden="true" is_update="0">
         <div class="modal-dialog">
            <div class="modal-content">
               <div class="modal-header">
                  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                  <h4 class="modal-title" id="page1_label">DAO生成向导</h4>
               </div>
               <div class="modal-body" style="position: relative;overflow: auto;width: auto;max-height:350px;">
                  <div class="steps step0 row-fluid">
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">数据库服务器：</label>
                           <Select id="servers" class="span7">
                              <option value="_please_select">--请选择--</option>
                           </Select>
                           <button id="del_server" type="button" class="btn btn-danger popup_text">删除选中</button>
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <button id="toggle_add_server" class="offset5 btn-primary fa fa-angle-down"></button>
                     </div>
                     <br>
                     <div id="add_server_row" class="row-fluid" style="display:none;">
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">驱动类：</label>
                           <input id="driver" type="text" class="span9 popup_text">
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">URL地址：</label>
                           <input id="url" type="text" class="span9 popup_text">
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">用户名：</label>
                           <input id="username" type="text" class="span9 popup_text">
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">密码：</label>
                           <input id="password" type="password" class="span9 popup_text">
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">数据库类型：</label>
                           <Select id="db_types" class="span9 popup_text">
                              <option value="_please_select">--请选择--</option>
                              <option value="mysql">My Sql</option>
                              <option value="sqlserver">Sql Server</option>
                           </Select>
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <button id="add_server" type="button" class="offset5 btn btn-primary">添加服务器</button>
                     </div>
                  </div>
                  </div>
                  <div class="steps step1 row-fluid">
                     <div class="control-group">
                        <label class="control-label popup_label">选择一个数据库：</label>
                        <Select id="databases" class="span9 popup_text">
                           <option value="_please_select">--请选择--</option>
                        </Select>
                     </div>
                  </div>
                  <div class="steps step2 row-fluid">
                     <div class="control-group">
                        <label class="control-label popup_label">DAO生成方式:</label>
                        <div class="btn-group popup_text span9" data-toggle="buttons">
                           <label class="gen_style btn btn-default active">
                           <input type="radio" name="dao_gen_style" id="dao_gen_style" value="auto" checked>自动生成SQL</label>
                           <label class="gen_style btn btn-default">
                           <input type="radio" name="dao_gen_style" id="dao_gen_style" value="sp">执行存储过程</label>
                           <label class="gen_style btn btn-default">
                           <input type="radio" name="dao_gen_style" id="dao_gen_style" value="sql">我要自己写查询</label>
                        </div>
                     </div>
                  </div>
                  <div class="steps step2-1-1 row-fluid">
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">选择一个表/视图：</label>
                           <Select id="tables" class="span9 popup_text">
                              <option value="_please_select">--请选择--</option>
                           </Select>
                        </div>
                     </div>
                     <div class="row-fluid">
                        <label class="popup_label"><input id="only_template" type="checkbox">仅生成DAO模板和Entity（无需任何编码，可以满足常规需求）</label>
                     </div>
                     <div class="row-fluid">
                        <label class="popup_label"><input id="cud_by_sp" type="checkbox" checked="true">增删改使用SPA或SP3（Sql Server请勾选，MySql请去除）</label>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">生成的类名：</label>
                           <input id="class_name" class="span9 popup_text">
                        </div>
                     </div>
                     <div class="row-fluid method_name_class">
                        <div class="control-group">
                           <label class="control-label popup_label">生成的方法名：</label>
                           <input id="method_name" class="span9 popup_text">
                        </div>
                     </div>
                     
                     <div class="row-fluid op_type_class">
                        <hr>
                        <div class="control-group">
                           <label class="control-label popup_label">操作类型：</label>
                           <div class="btn-group popup_text span9" data-toggle="buttons">
                              <label class="op_type btn btn-default active">
                              <input type="radio" name="operation_type" id="operation_type" value="select" checked>查询</label>
                              <label class="op_type btn btn-default">
                              <input type="radio" name="operation_type" id="operation_type" value="insert">新增</label>
                              <label class="op_type btn btn-default">
                              <input type="radio" name="operation_type" id="operation_type" value="update">修改</label>
                              <label class="op_type btn btn-default">
                              <input type="radio" name="operation_type" id="operation_type" value="delete">删除</label>
                           </div>
                        </div>
                     </div>
                  </div>
                  <div id="operation_fields" class="steps step2-1-3 row-fluid bootstrap-duallistbox-container">
                     <div id="select_fields">
                        <div class="span6 box1 filtered">
                           <div class="btn-group buttons">
                              <button type="button" class="btn moveall" title="Move all">
                              <i class="fa fa-arrow-right"></i>
                              <i class="fa fa-arrow-right"></i>
                              </button>
                              <button type="button" class="btn move" title="Move selected">
                              <i class="fa fa-arrow-right"></i>
                              </button>
                           </div>
                           <select id="fields_left" multiple="multiple">
                           </select>
                        </div>
                        <div class="span6 box1 filtered">
                           <div class="btn-group buttons">
                              <button type="button" class="btn remove" title="Remove selected"><i class="fa fa-arrow-left"></i>
                              </button>
                              <button type="button" class="btn removeall" title="Remove all">
                              <i class="fa fa-arrow-left"></i>
                              <i class="fa fa-arrow-left"></i>
                              </button>
                           </div>
                           <select id="fields_right" multiple="multiple">
                           </select>
                        </div>
                     </div>
                  </div>
                  <br>
                  <div id="where_condition" class="steps step2-1-3-add">
                     <div class="row-fluid">
                        <div class="span6">
                           <select id="fields_condition" class="span12">
                              <option value='-1'>--请选择--</option>
                           </select>
                        </div>
                        <div class="span6">
                           <select id="condition_values" class='span12'>
                              <option value='-1'>--请选择--</option>
                              <option value='0'>=</option>
                              <option value='1'>!=</option>
                              <option value='2'>&gl;</option>
                              <option value='3'>&lt;</option>
                              <option value='4'>&ge;</option>
                              <option value='5'>&le;</option>
                              <option value='6'>Between</option>
                              <option value='7'>Like</option>
                              <option value='8'>In</option>
                           </select>
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div class="offset3">
                           <input id="add_condition" type="button" class="btn btn-primary" value="Add a condition">
                           <input id="del_condition" type="button" class="btn btn-primary" value="Remove a condition">
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div class="span12">
                           <select class="span12" id="selected_condition" multiple="multiple">
                           </select>
                        </div>
                     </div>
                  </div>
                  <div class="steps step2-2 row-fluid" from="">
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">选择存储过程：</label>
                           <Select id="sps" class="span9 popup_text">
                              <option value="_please_select">--请选择--</option>
                           </Select>
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">执行的方式：</label>
                           <Select id="sp_type" class="span9 popup_text">
                              <option value="_please_select">--请选择--</option>
                              <option value="select">查询</option>
                              <option value="CUD">增删改</option>
                           </Select>
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div class="offset4">
                           <input id="view_sp_code" type="button" class="span5 btn btn-primary" value="View Sp Code">
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div id="sp_editor" class="span12">
                        </div>
                     </div>
                  </div>
                  <div class="steps step2-3 row-fluid" from="">
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">生成的类名：</label>
                           <input id="sql_class_name" class="span9 popup_text" type="text">
                        </div>
                     </div>
                     <div class="row-fluid">
                        <div class="control-group">
                           <label class="control-label popup_label">生成的方法名：</label>
                           <input id="sql_method_name" class="span9 popup_text" type="text">
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div id="sql_editor" class="span12">
                        </div>
                     </div>
                     <br>
                     <div class="row-fluid">
                        <div class="row-fluid">
                           <div class="span12">
                              <input id="variable" type="text" class="span3" value="">
                              <select id="variable_types" class="span3">
                                 <option value='_please_select'>--参数类型--</option>
                                 <option value='-7'>Bit</option>
                                 <option value='16'>Boolean</option>
                                 <option value='-6'>TinyInt</option>
                                 <option value='5'>SmallInt</option>
                                 <option value='4'>Integer</option>
                                 <option value='-5'>BigInt</option>
                                 <option value='6'>Float</option>
                                 <option value='7'>Real</option>
                                 <option value='8'>Double</option>
                                 <option value='2'>Numeric</option>
                                 <option value='3'>Decimal</option>
                                 <option value='1'>Char</option>
                                 <option value='12'>Varchar</option>
                                 <option value='-1'>LongVarchar</option>
                                 <option value='-15'>Nchar</option>
                                 <option value='-9'>NVarchar</option>
                                 <option value='-16'>LongNVarchar</option>
                                 <option value='91'>Date</option>
                                 <option value='92'>Time</option>
                                 <option value='93'>Timestamp</option>
                                 <option value='-2'>Binary</option>
                                 <option value='-3'>Varbinary</option>
                                 <option value='-4'>LongVarbinary</option>
                                 <option value='0'>Null</option>
                                 <option value='1111'>Other</option>
                                 <option value='2000'>JavaObject</option>
                                 <option value='2001'>Distinct</option>
                                 <option value='2002'>Struct</option>
                                 <option value='2003'>Array</option>
                                 <option value='2004'>Blob</option>
                                 <option value='2005'>Clob</option>
                                 <option value='2006'>Ref</option>
                                 <option value='70'>DataLink</option>
                                 <option value='-8'>Rowid</option>
                                 <option value='2011'>NClob</option>
                                 <option value='2009'>SqlXml</option>
                              </select>
                              <input id="variable_values" type="text" class="span4" value="">
                              <input id="add_variable" type="button" class="span2 btn btn-primary" value="添加">
                           </div>
                        </div>
                        <br>
                        <div class="row-fluid">
                           <div class="span12">
                              <select class="span10" id="selected_variable" multiple="multiple">
                              </select>
                              <input id="del_variable" type="button" class="span2 btn btn-danger" value="删除">
                           </div>
                        </div>
                        <br>
                        <input id="test_sql" type="button" class="offset3 span3 btn btn-primary" value="验证查询">
                     </div>
                  </div>
                  <div class="steps step3 row-fluid" from="">
                     <div class="control-group">
                        <label class="control-label popup_label">选择SQL风格：</label>
                        <Select id="sql_style" class="span9 popup_text">
                           <option value="csharp">C#风格(参数形式为@Name)</option>
                           <option value="java">JAVA风格(参数形式为?)</option>
                        </Select>
                     </div>
                  </div>
               </div>
               <div class="modal-footer">
                  <button id="prev_step"  type="button" class="btn btn-default">上一步</button>
                  <button id="next_step"  type="button" class="btn btn-primary">下一步</button>
                  <!-- <label class="popup_label"><input type="checkbox">保存时生成代码</label>
                     <button id="save_dao"  type="button" class="btn btn-primary">保存</button> -->
               </div>
            </div>
         </div>
         <!-- /.modal-content -->
      </div>
      <!-- /.modal-dialog -->
      <!--End wizard-->
      <!-- JS and analytics only. -->
      <!-- Bootstrap core JavaScript================================================== -->
      <!-- Placed at the end of the document so the pages load faster -->
      <script src="/static/jquery/jquery-1.10.2.min.js"></script>
      <script src="/static/bootstrap/js/bootstrap.min.js"></script>
      <script src="/static/w2ui/w2ui-1.3.js"></script>
      <script src="/static/jquery/jquery.blockui.min.js"></script>
      <script src="/static/js/sprintf.js"></script>
      <script src="/static/js/cblock.js"></script>
      <script src="/static/ace/ace.js"></script>
      <script src="/static/js/wizzard.js"></script>
      <script src="/static/js/index.js"></script>
   </body>
</html>
