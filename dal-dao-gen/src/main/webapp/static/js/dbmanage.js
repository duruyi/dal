
(function (window, undefined) {

    var Render = function () {

    };

    var refreshDB = function () {
        w2ui['grid'].clear();
        var current_group = w2ui['grid'].current_group;
        if (current_group == undefined) {
            if (w2ui['sidebar'].nodes.length < 1 || w2ui['sidebar'].nodes[0].nodes.length < 1)
                return;
            current_group = w2ui['sidebar'].nodes[0].nodes[0].id;
        }
        cblock($("body"));
        $.get("/rest/groupdb/groupdb?groupId=" + current_group + "&rand=" + Math.random(),function (data) {
            var allGroupDBs = [];
            $.each(data, function (index, value) {
                value.recid = allGroupDBs.length + 1;
                allGroupDBs.push(value);
            });
            w2ui['grid'].add(allGroupDBs);
            $("body").unblock();
        }).fail(function (data) {
                alert("获取所有Member失败!");
            });
    };

    var addDB = function(){
        $("#error_msg").html('');
        var current_group = w2ui['grid'].current_group;
        if(current_group==null || current_group==''){
            alert('请先选择Group');
            return;
        }
        ajaxutil.reload_dbservers();
        $("#comment").val('');
        $("#dbModal").modal({
            "backdrop": "static"
        });
    };

    var editDB = function(){
        $("#error_msg2").html('');
        var records = w2ui['grid'].getSelection();
        var record = w2ui['grid'].get(records[0]);
        if(record==null || record==''){
            alert("请先选择一个db");
            return;
        }
        $("#databases2").val(record["dbname"]);
        $("#comment2").val(record["comment"]);
        $("#dbModal2").modal({
            "backdrop": "static"
        });
    };

    var delDB = function(){
        var records = w2ui['grid'].getSelection();
        var record = w2ui['grid'].get(records[0]);
        if(record!=null){
            if (confirm("Are you sure to delete?")) {
                $.post("/rest/groupdb/delete", {
                    groupId : w2ui['grid'].current_group,
                    dbId : record['id']
                },function (data) {
                    if (data.code == "OK") {
                        refreshDB();
                    } else {
                        alert(data.info);
                    }
                }).fail(function (data) {
                        alert("执行异常");
                    });
            }
        }else{
            alert('请选择一个db！');
        }

    };

    var transferDB = function(){
        var current_group = w2ui['grid'].current_group;
        if(current_group==null || current_group==''){
            alert('请先选择Group');
            return;
        }
        $("#transferdb_error_msg").html("");
        var records = w2ui['grid'].getSelection();
        var record = w2ui['grid'].get(records[0]);
        if(record!=null){
            $.get("/rest/groupdb?root=true&rand=" + Math.random()).done(function (data) {

                if ($("#transferGroup")[0] != undefined && $("#transferGroup")[0].selectize != undefined) {
                    $("#transferGroup")[0].selectize.clearOptions();
                } else {
                    $("#transferGroup").selectize({
                        valueField: 'id',
                        labelField: 'title',
                        searchField: 'title',
                        sortField: 'title',
                        options: [],
                        create: false
                    });
                }

                var allGroups = [];
                $.each(data, function (index, value) {
                    allGroups.push({
                        id: value.id,
                        title: value['group_name']
                    });
                });
                $("#transferGroup")[0].selectize.addOption(allGroups);
                $("#transferGroup")[0].selectize.refreshOptions(false);

                $("body").unblock();
                $("#transferDbModal").modal();
            }).fail(function (data) {
                    alert('获取所有DAL Group失败.');
                    $("body").unblock();
                });
        }else{
            alert('请选择一个db！');
        }
    };

    Render.prototype = {
        render_layout: function (render_obj) {
            $(render_obj).w2layout({
                name: 'main_layout',
                panels: [{
                    type: 'left',
                    size: 271,
                    resizable: true,
                    style: 'border-right: 1px solid silver;'
                }, {
                    type: 'main'
                }],
                onResizing: function(event) {
                    //ace.edit("code_editor").resize();
                }   
            });
        },
        render_sidebar: function () {
            w2ui['main_layout'].content('left', '<div style="color: #34495E !important;font-size: 15px;background-color: #eee; padding: 7px 5px 6px 20px; border-bottom: 1px solid silver">'
                +'ALL DAL Team'
                +"</div>"
                +'<div id="jstree_groups"></div>');

            $('#jstree_groups').on('select_node.jstree', function (e, obj) {
                window.render.render_grid();
                w2ui['grid'].current_group = obj.node.id;
                w2ui['grid_toolbar'].click('refreshDB', null);
            }).jstree({ 
                'core' : {
                    'check_callback' : true,
                    'multiple': false,
                    'data' : {
                      'url' : function (node) {
                        return node.id == "#" ? "/rest/groupdb?root=true&rand=" + Math.random() : "/rest/groupdb?rand=" + Math.random();
                      }
                    }
            }});
        },
        render_grid: function (project_id) {
            var existsGrid = w2ui['grid'];
            if (existsGrid != undefined) {
                return;
            }

            w2ui['main_layout'].content('main', $().w2grid({
                name: 'grid',
                show: {
                    toolbar: true,
                    footer: true,
                    toolbarReload: false,
                    toolbarColumns: false,
                    //toolbarSearch: false,
                    toolbarAdd: false,
                    toolbarDelete: false,
                    //toolbarSave: true,
                    toolbarEdit: false
//                    selectColumn: true
                },
                multiSelect: false,
                toolbar: {
                    items: [{
                        type: 'break'
                    }, {
                        type: 'button',
                        id: 'refreshDB',
                        caption: '刷新',
                        icon: 'fa fa-refresh'
                    }, {
                        type: 'button',
                        id: 'addDB',
                        caption: '添加DB',
                        icon: 'fa fa-plus'
                    }, {
                        type: 'button',
                        id: 'editDB',
                        caption: '修改DB',
                        icon: 'fa fa-edit'
                    }, {
                        type: 'button',
                        id: 'delDB',
                        caption: '删除DB',
                        icon: 'fa fa-times'
                    }, {
                        type: 'button',
                        id: 'transferDB',
                        caption: '转移DB',
                        icon: 'fa fa-exchange'
                    }],
                    onClick: function (target, data) {
                        switch (target) {
                            case 'refreshDB':
                                refreshDB();
                                break;
                            case 'addDB':
                                addDB();
                                break;
                            case 'editDB':
                                editDB();
                                break;
                            case 'delDB':
                                delDB();
                                break;
                            case 'transferDB':
                                transferDB();
                                break;
                        }
                    }
                },
                searches: [{
                    field: 'dbname',
                    caption: 'DB Name',
                    type: 'text'
                }, {
                    field: 'comment',
                    caption: '备注',
                    type: 'text'
                }],
                columns: [{
                    field: 'dbname',
                    caption: 'DB Name',
                    size: '50%',
                    sortable: true,
                    attr: 'align=center'
                }, {
                    field: 'comment',
                    caption: '备注',
                    size: '50%',
                    sortable: true
                }],
                records: [],
                onDblClick: function (target, data) {
                }
            }));
        }
    };

    window.render = new Render();

    $('#main_layout').height($(document).height() - 50);

    window.render.render_layout($('#main_layout'));

    window.render.render_sidebar();

    window.render.render_grid();

    $(window).resize(function () {
        $('#main_layout').height($(document).height() - 50);
    });

    jQuery(document).ready(function(){

        var buildAddDbStr = function(){
            $("#error_msg3").html(" ");
            var mysqlConnStr = "Server=pub.mysql.db.dev.sh.ctripcorp.com;port=28747;UID=uws_allinone_1;password=ljwxg2yArv5uusoKpm9b;database=%s;";
            var sqlServerConnStr = "Data Source=devdb.dev.sh.ctriptravel.com,28747;UID=uws_AllInOneKey_dev;password=!QAZ@WSX1qaz2wsx;database=%s;";
            var addDdStr = "<add name=\"%s\" connectionString=\"%s\" providerName=\"System.Data.SqlClient\" />";

            var all_In_One_Name = $.trim($("#all_In_One_Name").val());
            var dbType = $.trim($("#dbtype").val());
            var realDB = $.trim($("#origianlDB").val());

            var result="";
            var connStr="";
            if("MySQL"==dbType){
                connStr = sprintf(mysqlConnStr,realDB);
                result = sprintf(addDdStr, all_In_One_Name, connStr);
            }else if("SQLServer"==dbType){
                connStr = sprintf(sqlServerConnStr,realDB);
                result = sprintf(addDdStr, all_In_One_Name, connStr);
            }else{
                result = sprintf(addDdStr, all_In_One_Name, "XXX");
            }
            $("#all_in_one").text(result);
        };

        $(document.body).on('change', "#dbtype", function(event){
//            if ($("#origianlDB")[0] != undefined && $("#origianlDB")[0].selectize != undefined) {
//                $("#origianlDB")[0].selectize.clearOptions();
//            } else {
//                $("#origianlDB").selectize({
//                    valueField: 'value',
//                    labelField: 'title',
//                    searchField: 'title',
//                    sortField: 'value',
//                    options: [],
//                    create: true
//                });
//            }
//            var dbType = $("#dbtype").val();
//            if("MySQL"==dbType){
//                $("#origianlDB")[0].selectize.addOption({
//                    value: "mysql",
//                    title: "mysql"
//                });
//            }else if("SQLServer"==dbType){
//                $("#origianlDB")[0].selectize.addOption({
//                    value: "sqlserver",
//                    title: "sqlserver"
//                });
//            }else{
//                $("#origianlDB")[0].selectize.clearOptions();
//            }
            buildAddDbStr();
        });

        $(document.body).on('keyup', "#origianlDB", function(event){
            var dbType = $.trim($("#dbtype").val());
            if("no"==dbType){
                $("#error_msg3").html("请先选择数据库类型");
                $("#origianlDB").val("");
            }else{
                buildAddDbStr();
            }
        });

        $(document.body).on('keyup', "#all_In_One_Name", function(event){
            buildAddDbStr();
        });

        $(document.body).on('click', "#add_db", function(event){
            var all_In_One_Name = $("#all_In_One_Name").val();
            var dbType = $("#dbtype").val();
            var realDB = $("#origianlDB").val();
            if(""==all_In_One_Name || null==all_In_One_Name){
                $("#error_msg3").html("请输入数据库的All In One的名称");
                return;
            }
            if("no"==dbType){
                $("#error_msg3").html("请选择数据库类型");
                return;
            }
            if(realDB==null || realDB==""){
                $("#error_msg3").html("请选择数据库");
                return;
            }

            cblock($("body"));
            $.post("/rest/db/all_in_one", {"data": $("#all_in_one").val()}, function(data){
                if(data.code == "OK"){
                    $("#manageDb").modal('hide');
                    window.ajaxutil.reload_dbservers();
                    $("#page1").modal();
                }else{
                    $("#error_msg3").html(data.info);
                }
                $("body").unblock();
            }).fail(function(data){
                    $("#error_msg3").text(data);
                    $("body").unblock();
                });;
        });

        $("#save_db").click(function(){
            var db_name = $("#databases").val();
            var comment = $("#comment").val();
            if(db_name==null || db_name==''){
                $("#error_msg").html('请选择DB!');
            }else{
                $.post("/rest/groupdb/add", {
                    groupId : w2ui['grid'].current_group,
                    dbname : db_name,
                    comment : comment,
                    gen_default_dbset:$("#gen_default_dbset").is(":checked")
                },function (data) {
                    if (data.code == "OK") {
                        $("#dbModal").modal('hide');
                        refreshDB();
                    } else {
                        $("#error_msg").html(data.info);
                    }
                }).fail(function (data) {
                        $("#error_msg").html(data.info);
                    });
            }
        });

        $("#update_db").click(function(){
            var records = w2ui['grid'].getSelection();
            var record = w2ui['grid'].get(records[0]);
            $.post("/rest/groupdb/update", {
                groupId : w2ui['grid'].current_group,
                dbId : record['id'],
                comment : $("#comment2").val()
            },function (data) {
                if (data.code == "OK") {
                    $("#dbModal2").modal('hide');
                    refreshDB();
                } else {
                    $("#error_msg2").html(data.info);
                }
            }).fail(function (data) {
                    $("#error_msg2").html(data.info);
                });
        });

        $("#transfer_db").click(function(){
            var records = w2ui['grid'].getSelection();
            var record = w2ui['grid'].get(records[0]);
            var dbId = record['id'];
            var groupId = $("#transferGroup").val();
            $("#transferdb_error_msg").html("");
            if(dbId==null||dbId==''){
                $("#transferdb_error_msg").html("请选择需要转移的DataBase.");
                return;
            }
            if(groupId==null||groupId==''){
                $("#transferdb_error_msg").html("请选择需要转入的DAL Team.");
                return;
            }
            $.post("/rest/groupdb/transferdb", {
                groupId : groupId,
                dbId : dbId
            },function (data) {
                if (data.code == "OK") {
                    $("#transferDbModal").modal('hide');
                    refreshDB();
                } else {
                    $("#transferdb_error_msg").html(data.info);
                }
            }).fail(function (data) {
                    $("#transferdb_error_msg").html(data.info);
                });
        });
    });

})(window);