
(function (window, undefined) {
    var sql_builder = function () {

    };

    var whereCondition = {
        "0": "=",
        "1": "!=",
        "2": ">",
        "3": "<",
        "4": ">=",
        "5": "<=",
        "6": "Between",
        "7": "Like",
        "8": "In"
    };

    sql_builder.prototype = {
        build: function () {
            var formatedConditions = [];
            $("#selected_condition > option").each(function () {
                var splited = this.value.split(",");
                if (splited.length >= 2) {
                    //between
                    if (splited[1] == "6") {
                        if ($("#sql_style").val() == "csharp") {
                            formatedConditions.push(sprintf(
                                " %s BETWEEN @%s_start AND @%s_end ",splited[0], splited[0], splited[0]));
                        } else {
                            formatedConditions.push(sprintf(" %s BETWEEN ? AND ? ",splited[0]));
                        }
                    } else {
                        if ($("#sql_style").val() == "csharp") {
                            formatedConditions.push(sprintf(
                                " %s %s @%s ", splited[0], whereCondition[splited[1]],
                                splited[0]));
                        } else {
                            formatedConditions.push(sprintf(" %s %s ? ",
                                splited[0], whereCondition[splited[1]]));
                        }
                    }
                }
            });
            if ($("#crud_option").val() == "select") {
                var select_sql_builder = '';
                if("java"==$("#sql_style").val()){
                    if (formatedConditions.length > 0) {
                        select_sql_builder = sprintf("SELECT %s FROM %s WHERE %s", $('#fields').multipleSelect('getSelects').join(","),
                            $("#tables").val(), formatedConditions.join(" AND "));
//                        ace.edit("sql_builder").setValue(sprintf("SELECT %s FROM %s WHERE %s", $('#fields').multipleSelect('getSelects').join(","),
//                            $("#tables").val(), formatedConditions.join(" AND ")));
                    } else {
                        select_sql_builder = sprintf("SELECT %s FROM %s", $('#fields').multipleSelect('getSelects').join(","),
                            $("#tables").val());
//                            ace.edit("sql_builder").setValue(sprintf("SELECT %s FROM %s", $('#fields').multipleSelect('getSelects').join(","),
//                            $("#tables").val()));
                    }
                }else{
                    if (formatedConditions.length > 0) {
                        select_sql_builder = sprintf("SELECT %s FROM %s WHERE %s", $('#fields').multipleSelect('getSelects').join(","),
                            $("#tables").val(), formatedConditions.join(" AND "));
//                        ace.edit("sql_builder").setValue(sprintf("SELECT %s FROM %s WHERE %s", $('#fields').multipleSelect('getSelects').join(","),
//                            $("#tables").val(), formatedConditions.join(" AND ")));
                    } else {
                        select_sql_builder = sprintf("SELECT %s FROM %s", $('#fields').multipleSelect('getSelects').join(","),
                            $("#tables").val());
//                        ace.edit("sql_builder").setValue(sprintf("SELECT %s FROM %s", $('#fields').multipleSelect('getSelects').join(","),
//                            $("#tables").val()));
                    }
                }
                if($("#orderby_field").val() != '-1'){
                    select_sql_builder = sprintf("%s ORDER BY %s %s",select_sql_builder,$("#orderby_field").val(),$("#orderby_sort").val());
                }
                ace.edit("sql_builder").setValue(select_sql_builder);
            } else if ($("#crud_option").val() == "insert") {

                var placeHodler = [];
                $.each($('#fields').multipleSelect('getSelects'), function (index, value) {
                    if ($("#sql_style").val() == "csharp") {
                        placeHodler.push(sprintf(" @%s ", value));
                    } else {
                        placeHodler.push(" ? ");
                    }
                });
                ace.edit("sql_builder").setValue(sprintf("INSERT INTO %s (%s) VALUES (%s)",
                    $("#tables").val(), $('#fields').multipleSelect('getSelects').join(","),
                    placeHodler.join(",")));
            } else if ($("#crud_option").val() == "update") {
                var placeHodler = [];
                $.each($('#fields').multipleSelect('getSelects'), function (index, value) {
                    if ($("#sql_style").val() == "csharp") {
                        placeHodler.push(sprintf(" %s = @%s ", value, value));
                    } else {
                        placeHodler.push(sprintf(" %s = ? ", value));
                    }
                });

                if (formatedConditions.length > 0) {
                    ace.edit("sql_builder").setValue(sprintf("UPDATE %s SET %s WHERE %s",
                        $("#tables").val(),
                        placeHodler.join(","),
                        formatedConditions.join(" AND ")));
                } else {
                    ace.edit("sql_builder").setValue(sprintf("UPDATE %s SET %s ",
                        $("#tables").val(),
                        placeHodler.join(",")));
                }

            } else if ($("#crud_option").val() == "delete") {
                if (formatedConditions.length > 0) {
                    ace.edit("sql_builder").setValue(sprintf("Delete FROM %s WHERE %s",
                        $("#tables").val(),
                        formatedConditions.join(" AND ")));
                } else {
                    ace.edit("sql_builder").setValue(sprintf("Delete FROM %s", $("#tables").val()));
                }
            }
        }
    };

    /**
     * export to either browser or node.js
     */
    window.sql_builder = new sql_builder();

})(window);