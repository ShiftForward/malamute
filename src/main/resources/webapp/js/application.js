// Models
window.Project = Backbone.Model.extend();
 
window.Deploy = Backbone.Model.extend();

window.ProjectCollection = Backbone.Collection.extend({
    model : Project,
    url : "/projects"
});
 
window.ProjectModel = Backbone.Collection.extend({
    model : Project, 
    initialize: function(id) {
       this.url = "/project/"+id;
    }
});

window.DeployModel = Backbone.Collection.extend({
    model : Deploy, 
    initialize: function(proj,id) {
       this.url = "/project/" + proj + "/deploy/" + id;
    }
});

window.DeployCollection = Backbone.Collection.extend({
    model : Deploy, 
    initialize: function(id) {
       this.url = "/project/"+id+"/deploys";
    }
}); 

// Views
var deploysTableHeader = "<thead>"+
            "<tr>"+
              "<th>Timestamp</th>"+
              "<th>Description</th>"+
              "<th>Git</th>"+
              "<th>Version</th>"+
              "<th>User</th>"+
              "<th>Changelog</th>"+
              "<th>Client</th>"+
              "<th>Details</th>"+
            "</tr>"+
          "</thead>";

window.ProjectListView = Backbone.View.extend({
 
    initialize:function () {
        this.model.bind("reset", this.render, this);
    },
 
    render:function (eventName) {
		this.model.models.forEach(function(proj) {
		 	$(this.el).append(new ProjectListItemView({model:proj.attributes}).render().el);
		}, this);
        return this;
    }
 
});
 
window.ProjectListItemView = Backbone.View.extend({
 
    template:_.template($('#projectListTpl').html()),
 
    render:function (eventName) {
        $(this.el).html(this.template(this.model));
        return this;
    }
 
});

window.DeployListView = Backbone.View.extend({
    tagName: 'tbody',

    initialize:function (options) {
        this.model.bind("reset", this.render, this);
        this.projname = options.proj
    },
 
    render:function (eventName) {
		this.model.models.forEach(function(deploy) {
            var currentDeploy = deploy.attributes;
            currentDeploy.projname = this.projname;
       
		 	$(this.el).append(new DeployListItemView({model:currentDeploy}).render().el);
		}, this);
        return this;
    }
 
});
 
window.DeployListItemView = Backbone.View.extend({
    tagName: 'tr',
    
    template:_.template($('#deployTpl').html()),
    
    render:function (eventName) {
        var deploy = this.model;
        deploy.timestamp = timeConverter(deploy.timestamp);
        if(deploy.events[deploy.events.length - 1].status === "SUCCESS")
                this.className = "success";
            else if(deploy.events[deploy.events.length - 1].status === "FAILED")
                this.className = "danger";
            else if(deploy.events[deploy.events.length - 1].status === "SKIPPED")
                this.className = "warning";
            else
                this.className = "";
        $(this.el).html(this.template(deploy));
        return this;
    }
 
});

window.DeployView = Backbone.View.extend({
 
    initialize:function (options) {
        this.model.bind("reset", this.render, this);
        this.projname = options.proj
    },
    
    template:_.template($('#deployDetailTpl').html()),
 
    render:function (eventName) {
        deploy = this.model.models[0].attributes;
        deploy.projname = this.projname;
        events = new Array();
        deploy.events.reverse().forEach(function(ev) {
             ev.timestamp = timeConverter(ev.timestamp);
             events.push(ev);
        }, this);
        deploy.timestamp = timeConverter(deploy.timestamp);
        $(this.el).html(this.template(deploy));
        return this;
    }
 
});
 
window.ProjectView = Backbone.View.extend({
 
    template:_.template($('#projectTpl').html()),
 
    render:function (eventName) {
        $(this.el).html(this.template(this.model.models[0].attributes));
        return this;
    }
 
});
 
// Router
var AppRouter = Backbone.Router.extend({
 
    routes: {
		"" 				: "home",
        ":projname"		: "project",
        ":project/:id"	: "deploy"
    },
	 
    home: function () {
        $('.deploy-section').hide();
        ProjectList = new ProjectCollection();
        ProjectList.fetch({
            reset:"true",
            success:  (function () {
                this.ProjectListView = new ProjectListView({model: this.ProjectList});
                $('.project-section').html(this.ProjectListView.render().el);
            })
        });
       
    },
 
    project: function (projname) {
        proj = new ProjectModel(projname);
        proj.fetch({
            reset:"true",
            success:  (function () {
                this.projView = new ProjectView({model: proj});
                $('.project-section').html(this.projView.render().el);
            })
        });

        deployList = new DeployCollection(projname);
        deployList.fetch({
            reset:"true",
            success:  (function () {
                this.deployListView = new DeployListView({model: this.deployList, proj: projname});
                $('.event-section').hide();
                $('.deploy-section').show();
                $('.deploys').html("");
                $('.deploys').append(deploysTableHeader);
                $('.deploys').append(this.deployListView.render().el);
            })
        });
    },
 
    deploy: function (projname, id) {
        proj = new ProjectModel(projname);
        proj.fetch({
            reset:"true",
            success:  (function () {
                this.projView = new ProjectView({model: proj});
                $('.project-section').html(this.projView.render().el);
            })
        });
        deployDetails = new DeployModel(projname,id);
        deployDetails.fetch({
            reset:"true",
            success:  (function () {
                this.deployDetailsView = new DeployView({model: this.deployDetails, proj: projname});
                $('.deploy-section').hide();
                $('.event-section').show();
                $('.event-section').html(this.deployDetailsView.render().el);
            })
        });
    }
});
 
var app = new AppRouter();
Backbone.history.start();

function timeConverter(timestamp) {
    var d = new Date(timestamp),	// Convert the passed timestamp to milliseconds
        yyyy = d.getFullYear(),
        mm = ('0' + (d.getMonth() + 1)).slice(-2),	// Months are zero based. Add leading 0.
        dd = ('0' + d.getDate()).slice(-2),			// Add leading 0.
        hh = d.getHours(),
        h = hh,
        min = ('0' + d.getMinutes()).slice(-2),		// Add leading 0.
        time;

    time = dd + '-' + mm + '-' + yyyy + ', ' + hh + ':' + min;

    return time;
}