(function() {
	Backbone.d3.couch = {
		PlotCollection : Backbone.couch.Collection.extend({
			initialize : Backbone.d3.PlotCollection.initialize
		})
	}
})(); 