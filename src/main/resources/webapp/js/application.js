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
    
    initialize:function () {
        this.model.bind("reset", this.render, this);
    },
 
    render:function (eventName) {
		this.model.models.forEach(function(deploy) {
            var currentDeploy = deploy.attributes;
            currentDeploy.projname = this.proj;
		 	$(this.el).append(new DeployListItemView({model:currentDeploy}).render().el);
		}, this);
        return this;
    }
 
});
 
window.DeployListItemView = Backbone.View.extend({
    tagName: 'tr',
    
    template:_.template($('#deployTpl').html()),
    
    render:function (eventName) {
        var deploy = this.model
        deploy.timestamp = timeConverter(deploy.timestamp)
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
 
    template:_.template($('#deployDetailTpl').html()),
 
    render:function (eventName) {
        console.log(this.model.models[0])
        deploy = this.model.models[0].attributes;
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
        this.ProjectList = new ProjectCollection();
        this.ProjectListView = new ProjectListView({model: this.ProjectList});
        this.ProjectList.fetch({async:false});
        $('.project-section').html(this.ProjectListView.render().el);
    },
 
    project: function (projname) {
        console.log(projname)
        this.proj = new ProjectModel(projname);
        this.projView = new ProjectView({model: this.proj});
        this.proj.fetch({async:false});
        $('.project-section').html(this.projView.render().el);
        this.deployList = new DeployCollection(projname);
        this.deployListView = new DeployListView({model: this.deployList, proj: projname});
        this.deployList.fetch({async:false});
        
        $('.deploy-section').show();
        $('.deploys').append(this.deployListView.render().el);
    },
 
    deploy: function (projname, id) {
       $('.deploy-section').hide();
       this.proj = new ProjectModel(projname);
        this.projView = new ProjectView({model: this.proj});
        this.proj.fetch({async:false});
        $('.project-section').html(this.projView.render().el);
        this.deployDetails = new DeployModel(projname,id);
        this.deployDetailsView = new DeployView({model: this.deployDetails, proj: projname});
        this.deployDetails.fetch({async:false});
        $('.event-section').append(this.deployDetailsView.render().el);
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