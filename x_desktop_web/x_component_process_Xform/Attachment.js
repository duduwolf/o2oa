MWF.xDesktop.requireApp("process.Xform", "$Module", null, false);
MWF.xApplication.process.Xform.Attachment = MWF.APPAttachment =  new Class({
	Extends: MWF.APP$Module,
    options: {
        "moduleEvents": ["upload", "delete"]
    },

    initialize: function(node, json, form, options){
        this.node = $(node);
        this.node.store("module", this);
        this.json = json;
        this.form = form;
        this.field = true;
    },

	_loadUserInterface: function(){
		this.node.empty();
        this.loadAttachmentController();
	},
    loadAttachmentController: function(){
        debugger;
        MWF.require("MWF.widget.AttachmentController", function() {
            var options = {
                "title": "附件区域",
                "listStyle": this.json.listStyle || "icon",
                "size": this.json.size || "max",
                "resize": (this.json.size=="true") ? true : false,
                "attachmentCount": this.json.attachmentCount || 0,
                "isUpload": (this.json.isUpload=="y") ? true : false,
                "isDelete": (this.json.isDelete=="y") ? true : false,
                "isReplace": (this.json.isReplace=="y") ? true : false,
                "isDownload": (this.json.isDownload=="y") ? true : false,
                "isSizeChange": (this.json.isSizeChange=="y") ? true : false,
                "readonly": (this.json.readonly=="y") ? true : false
            }
            if (this.readonly) options.readonly = true;
            this.attachmentController = new MWF.widget.ATTER(this.node, this, options);
            this.attachmentController.load();

            this.form.businessData.attachmentList.each(function (att) {
                if (att.site==this.json.id) this.attachmentController.addAttachment(att);
            }.bind(this));
        }.bind(this));
    },

    _loadEvents: function(editorConfig){
        Object.each(this.json.events, function(e, key){
            if (e.code){
                if (this.options.moduleEvents.indexOf(key)!=-1){
                    this.addEvent(key, function(event){
                        return this.form.Macro.fire(e.code, this, event);
                    }.bind(this));
                }else{
                    this.node.addEvent(key, function(event){
                        return this.form.Macro.fire(e.code, this, event);
                    }.bind(this));
                }
            }
        }.bind(this));

    },
    getData: function(){
        return this.attachmentController.getAttachmentNames();
    },
    createUploadFileNode: function(){
        this.uploadFileAreaNode = new Element("div");
        var html = "<input name=\"file\" type=\"file\" multiple/>";
        this.uploadFileAreaNode.set("html", html);

        this.fileUploadNode = this.uploadFileAreaNode.getFirst();
        this.fileUploadNode.addEvent("change", function(){

            var files = this.fileUploadNode.files;
            if (files.length){
                if ((files.length+this.attachmentController.attachments.length > this.attachmentController.options.attachmentCount) && this.attachmentController.options.attachmentCount>0){
                    var content = MWF.xApplication.process.Xform.LP.uploadMore;
                    content = content.replace("{n}", this.attachmentController.options.attachmentCount);
                    this.form.notice(content, "error");
                }else{
                    for (var i = 0; i < files.length; i++) {
                        var file = files.item(i);

                        var formData = new FormData();
                        formData.append('file', file);
                        formData.append('site', this.json.id);
                        //formData.append('folder', folderId);

                        this.form.workAction.uploadAttachment(this.form.businessData.work.id ,function(o, text){
                            if (o.id){
                                this.form.workAction.getAttachment(o.id, this.form.businessData.work.id, function(json){
                                    if (json.data) this.attachmentController.addAttachment(json.data);
                                    this.attachmentController.checkActions();

                                    this.fireEvent("upload", [json.data]);
                                }.bind(this))
                            }
                            this.attachmentController.checkActions();
                        }.bind(this), null, formData, file);
                    }
                }
            }
        }.bind(this));
    },
    uploadAttachment: function(e, node){
        if (!this.uploadFileAreaNode){
            this.createUploadFileNode();
        }
        //var fileNode = this.uploadFileAreaNode.getFirst();
        this.fileUploadNode.click();
    },
    deleteAttachments: function(e, node, attachments){
        var names = [];
        attachments.each(function(attachment){
            names.push(attachment.data.name);
        }.bind(this));

        var _self = this;
        this.form.confirm("warn", e, MWF.xApplication.process.Xform.LP.deleteAttachmentTitle, MWF.xApplication.process.Xform.LP.deleteAttachment+"( "+names.join(", ")+" )", 300, 120, function(){
            while (attachments.length){
                attachment = attachments.shift();
                _self.deleteAttachment(attachment);
            }
            this.close();
        }, function(){
            this.close();
        }, null);
    },
    deleteAttachment: function(attachment){
        this.fireEvent("delete", [attachment.data]);
        this.form.workAction.deleteAttachment(attachment.data.id, this.form.businessData.work.id, function(josn){
            this.attachmentController.removeAttachment(attachment);
            this.attachmentController.checkActions();
        }.bind(this));
    },

    replaceAttachment: function(e, node, attachment){
        var _self = this;
        this.form.confirm("warn", e, MWF.xApplication.process.Xform.LP.replaceAttachmentTitle, MWF.xApplication.process.Xform.LP.replaceAttachment+"( "+attachment.data.name+" )", 300, 120, function(){
            _self.replaceAttachmentFile(attachment);
            this.close();
        }, function(){
            this.close();
        }, null);
    },

    createReplaceFileNode: function(attachment){
        this.replaceFileAreaNode = new Element("div");
        var html = "<input name=\"file\" type=\"file\" multiple/>";
        this.replaceFileAreaNode.set("html", html);

        this.fileReplaceNode = this.replaceFileAreaNode.getFirst();
        this.fileReplaceNode.addEvent("change", function(){

            var files = this.fileReplaceNode.files;
            if (files.length){
                for (var i = 0; i < files.length; i++) {
                    var file = files.item(i);

                    var formData = new FormData();
                    formData.append('file', file);
                //    formData.append('site', this.json.id);

                    this.form.workAction.replaceAttachment(attachment.data.id, this.form.businessData.work.id ,function(o, text){
                        this.form.workAction.getAttachment(attachment.data.id, this.form.businessData.work.id, function(json){
                            attachment.data = json.data;
                            attachment.reload();
                            this.attachmentController.checkActions();
                        }.bind(this))
                    }.bind(this), null, formData, file);
                }
            }
        }.bind(this));
    },

    replaceAttachmentFile: function(attachment){
        if (!this.replaceFileAreaNode){
            this.createReplaceFileNode(attachment);
        }
        this.fileReplaceNode.click();
    },
    downloadAttachment: function(e, node, attachments){
        if (this.form.businessData.work){
            attachments.each(function(att){
                debugger;
                this.form.workAction.getAttachmentStream(att.data.id, this.form.businessData.work.id);
            }.bind(this));
        }else{
            attachments.each(function(att){
                this.form.workAction.getWorkcompletedAttachmentStream(att.data.id, this.form.businessData.workCompleted.id);
            }.bind(this));
        }
    },
    openAttachment: function(e, node, attachments){
        if (this.form.businessData.work){
            attachments.each(function(att){
                this.form.workAction.getAttachmentData(att.data.id, this.form.businessData.work.id);
            }.bind(this));
        }else{
            attachments.each(function(att){
                this.form.workAction.getWorkcompletedAttachmentData(att.data.id, this.form.businessData.workCompleted.id);
            }.bind(this));
        }
        //this.downloadAttachment(e, node, attachment);
    },
    getAttachmentUrl: function(attachment, callback){
        if (this.form.businessData.work){
            this.form.workAction.getAttachmentUrl(attachment.data.id, this.form.businessData.work.id, callback);
        }else{
            this.form.workAction.getAttachmentWorkcompletedUrl(attachment.data.id, this.form.businessData.workCompleted.id, callback);
        }
    },
    createErrorNode: function(text){
        var node = new Element("div");
        var iconNode = new Element("div", {
            "styles": {
                "width": "20px",
                "height": "20px",
                "float": "left",
                "background": "url("+"/x_component_process_Xform/$Form/default/icon/error.png) center center no-repeat"
            }
        }).inject(node);
        var textNode = new Element("div", {
            "styles": {
                "line-height": "20px",
                "margin-left": "20px",
                "color": "red"
            },
            "text": text
        }).inject(node);
        return node;
    },
    notValidationMode: function(text){
        if (!this.isNotValidationMode){
            this.isNotValidationMode = true;
            this.node.store("borderStyle", this.node.getStyles("border-left", "border-right", "border-top", "border-bottom"));
            this.node.setStyle("border", "1px solid red");

            this.errNode = this.createErrorNode(text).inject(this.node, "after");
            this.showNotValidationMode(this.node);
        }
    },
    showNotValidationMode: function(node){
        var p = node.getParent("div");
        if (p){
            if (p.get("MWFtype") == "tab$Content"){
                if (p.getParent("div").getStyle("display")=="none"){
                    var contentAreaNode = p.getParent("div").getParent("div");
                    var tabAreaNode = contentAreaNode.getPrevious("div");
                    var idx = contentAreaNode.getChildren().indexOf(p.getParent("div"));
                    var tabNode = tabAreaNode.getChildren()[idx];
                    tabNode.click();
                    p = tabAreaNode.getParent("div");
                }
            }
            this.showNotValidationMode(p);
        }
    },
    validationMode: function(){
        if (this.isNotValidationMode){
            this.isNotValidationMode = false;
            this.node.setStyles(this.node.retrieve("borderStyle"));
            if (this.errNode){
                this.errNode.destroy();
                this.errNode = null;
            }
        }
    },
    validationConfigItem: function(routeName, data){
        var flag = (data.status=="all") ? true: (routeName == data.decision);
        if (flag){
            var n = this.getData();
            var v = (data.valueType=="value") ? n : n.length;
            switch (data.operateor){
                case "isnull":
                    if (!v){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "notnull":
                    if (v){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "gt":
                    if (v>data.value){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "lt":
                    if (v<data.value){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "equal":
                    if (v==data.value){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "neq":
                    if (v!=data.value){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "contain":
                    if (v.indexOf(data.value)!=-1){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
                case "notcontain":
                    if (v.indexOf(data.value)==-1){
                        this.notValidationMode(data.prompt);
                        return false;
                    }
                    break;
            }
        }
        return true;
    },
    validationConfig: function(routeName, opinion){
        debugger;
        if (this.json.validationConfig){
            if (this.json.validationConfig.length){
                for (var i=0; i<this.json.validationConfig.length; i++) {
                    var data = this.json.validationConfig[i];
                    if (!this.validationConfigItem(routeName, data)) return false;
                }
            }
            return true;
        }
        return true;
    },
    validation: function(routeName, opinion){
        if (!this.validationConfig(routeName, opinion))  return false;

        if (!this.json.validation) return true;
        if (!this.json.validation.code) return true;
        var flag = this.form.Macro.exec(this.json.validation.code, this);
        if (!flag) flag = MWF.xApplication.process.Xform.LP.notValidation;
        if (flag.toString()!="true"){
            this.notValidationMode(flag);
            return false;
        }
        return true;
    }

}); 