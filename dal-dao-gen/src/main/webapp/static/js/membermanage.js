
(function (window, undefined) {

    var Render = function () {

    };

    var refreshMember = function () {
        w2ui['grid'].clear();
        var current_project = w2ui['grid'].current_project;
        if (current_project == undefined) {
            if (w2ui['sidebar'].nodes.length < 1 || w2ui['sidebar'].nodes[0].nodes.length < 1)
                return;
            current_project = w2ui['sidebar'].nodes[0].nodes[0].id;
        }
        cblock($("body"));
        $.get("/rest/task?project_id=" + current_project + "&rand=" + Math.random(),function (data) {
            var allTasks = [];
            $.each(data.tableViewSpTasks, function (index, value) {
                value.recid = allTasks.length + 1;
                value.task_type = "table_view_sp";
                value.task_desc = "表/视图/存储过程";
                if (value.table_names != null && value.table_names != "") {
                    value.sql_content = value.table_names;
                }
                if (value.sp_names != null && value.sp_names != "") {
                    if (value.sql_content == null || value.sql_content == "")
                        value.sql_content = value.sp_names;
                    else
                        value.sql_content = value.sql_content + "," + value.sp_names;
                }
                if (value.view_names != null && value.view_names != "") {
                    if (value.sql_content == null || value.sql_content == "")
                        value.sql_content = value.view_names;
                    else
                        value.sql_content = value.sql_content + "," + value.view_names;
                }
                value.class_name = "/";
                value.method_name = "/";
                allTasks.push(value);
            });
            $.each(data.autoTasks, function (index, value) {
                value.recid = allTasks.length + 1;
                value.task_type = "auto";
                value.task_desc = "SQL构建";
                value.class_name = value.table_name;
                allTasks.push(value);
            });
            $.each(data.sqlTasks, function (index, value) {
                value.recid = allTasks.length + 1;
                value.task_type = "sql";
                value.task_desc = "自定义查询";
                allTasks.push(value);
            });
            w2ui['grid'].add(allTasks);
            $("body").unblock();
        }).fail(function (data) {
                alert("获取所有Member失败!");
            });
    };

    var addMember = function(){
        $("#memberModal").modal({
            "backdrop": "static"
        });
        $("#save_member").click(function(){
            var userNo = $("userNo").val();
            var comment = $("comment").val();
            if(userNo==null){
                $("#error_msg").html('请输入Group Name!');
            }
        });
    };

    var editMember = function(){
        $("#memberModal").modal({
            "backdrop": "static"
        });
    };

    var delMember = function(){
        var records = w2ui['grid'].getSelection();
        var record = w2ui['grid'].get(records[0]);
        if(record!=null){
            if (confirm("Are you sure to delete?")) {

            }
        }else{
            alert('请选择一个member！');
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
                +'All DAL Team'
                +"</div>"
                +'<div id="jstree_projects"></div>');

            $('#jstree_projects').on('select_node.jstree', function (e, obj) {
                if(obj.node.id != -1){
                    window.render.render_grid();

                    w2ui['grid'].current_project = obj.node.id;
                    w2ui['grid_toolbar'].click('refreshDAO', null);
                }
            }).jstree({ 
                'core' : {
                    'check_callback' : true,
                    'multiple': false,
                    'data' : {
                      'url' : function (node) {
                        return node.id == "#" ? "/rest/member?root=true&rand=" + Math.random() : "/rest/member?rand=" + Math.random();
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
                },
                toolbar: {
                    items: [{
                        type: 'break'
                    }, {
                        type: 'button',
                        id: 'refreshMember',
                        caption: '刷新',
                        icon: 'fa fa-refresh'
                    }, {
                        type: 'button',
                        id: 'addMember',
                        caption: '添加Member',
                        icon: 'fa fa-plus'
                    }, {
                        type: 'button',
                        id: 'editMember',
                        caption: '修改Member',
                        icon: 'fa fa-edit'
                    }, {
                        type: 'button',
                        id: 'delMember',
                        caption: '删除Member',
                        icon: 'fa fa-times'
                    }],
                    onClick: function (target, data) {
                        switch (target) {
                            case 'refreshMember':
                                refreshMember();
                                break;
                            case 'addMember':
                                addMember();
                                break;
                            case 'editMember':
                                editMember();
                                break;
                            case 'delMember':
                                delMember();
                                break;
                        }
                    }
                },
                searches: [{
                    field: 'db_name',
                    caption: 'Member Name',
                    type: 'text'
                }, {
                    field: 'table_name',
                    caption: 'Member Email',
                    type: 'text'
                }],
                columns: [{
                    field: 'db_name',
                    caption: 'Member Name',
                    size: '50%',
                    sortable: true,
                    attr: 'align=center'
                }, {
                    field: 'class_name',
                    caption: 'Member Email',
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

})(window);