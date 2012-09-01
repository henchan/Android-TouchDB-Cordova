(function() {

  Backbone.d3.Canned["Calendar"] = {
    View: Backbone.d3.PlotView.extend({
      day: d3.time.format("%w"),
      week: d3.time.format("%U"),
      percent: d3.format(".1%"),
      format: d3.time.format("%Y-%m-%d"),
      m: [19, 20, 20, 19],
      gw: 700,
      gh: 136,
      z: 10,
      plot: function(options) {
        var color, data, h, rect, svg, that, w;
        that = this;
        w = 700 - this.m[1] - this.m[3];
        h = 136 - this.m[0] - this.m[2];
        color = d3.scale.quantize().domain([0, 1]).range(d3.range(9));
        svg = this.div.selectAll("svg").data(d3.range(1985, 2011)).enter().append("svg:svg").attr("width", w + this.m[1] + this.m[3]).attr("height", h + this.m[0] + this.m[2]).attr("class", "RdYlGn").append("svg:g").attr("transform", "translate(" + (this.m[3] + (w - this.z * 53) / 2) + "," + (this.m[0] + (h - this.z * 7) / 2) + ")");
        svg.append("svg:text").attr("transform", "translate(-6," + this.z * 3.5 + ")rotate(-90)").attr("text-anchor", "middle").text(String);
        rect = svg.selectAll("rect.day").data(function(d) {
          return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1));
        }).enter().append("svg:rect").attr("class", "day").attr("width", this.z).attr("height", this.z).attr("x", function(d) {
          return that.week(d) * that.z;
        }).attr("y", function(d) {
          return that.day(d) * that.z;
        });
        svg.selectAll("path.month").data(function(d) {
          return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1));
        }).enter().append("svg:path").attr("class", "month").attr("d", that.monthPath);
        data = d3.nest().key(function(d) {
          return d.date;
        }).rollup(function(d) {
          return d[0].count;
        }).map(this.plotdata());
        return rect.attr("class", function(d) {
          return "day q" + color(data[that.format(d)]) + "-9";
        }).append("svg:title").text(function(d) {
          return (d = that.format(d)) + (d in data ? ": " + that.percent(data[d]) : "");
        });
      },
      plotdata: function() {
        var data, max;
        data = [];
        max = this.collection.max(function(d) {
          return d.get("count");
        }).get("count");
        this.collection.forEach(function(datapoint) {
          return data.push({
            date: datapoint.get("date"),
            count: 1 - parseFloat(datapoint.get("count")) / max
          });
        });
        return data;
      },
      monthPath: function(t0) {
        var d0, d1, t1, w0, w1;
        t1 = new Date(t0.getUTCFullYear(), t0.getUTCMonth() + 1, 0);
        d0 = +this.day(t0);
        w0 = +this.week(t0);
        d1 = +this.day(t1);
        w1 = +this.week(t1);
        return "M" + (w0 + 1) * this.z + "," + d0 * this.z + "H" + w0 * this.z + "V" + 7 * this.z + "H" + w1 * this.z + "V" + (d1 + 1) * this.z + "H" + (w1 + 1) * this.z + "V" + 0 + "H" + (w0 + 1) * this.z + "Z";
      }
    }),
    Model: Backbone.Model.extend({
      initialize: function(data) {
        this.set({
          date: data.date,
          count: data.count
        });
        return {
          _db: Backbone.couch.db('flirtigo_db'),
          defaults: {
            type: 'calendar'
          }
        };
      }
    }),
    Collection: Backbone.d3.couch.PlotCollection.extend({
      _db: Backbone.couch.db('flirtigo_db'),
      change_feed: true,
      couch: function() {
        return {
          view: 'flirtigo/type',
          key: 'flapp',
          include_docs: true
        };
      },
      model: this.Model,
      success: function(result, foo) {
        var models;
        models = [];
        _.each(result, function(row) {
          return models.push(new CalDataPoint(row.value));
        });
        if (models.length === 0) models = null;
        return models;
      }
    })
  };

}).call(this);
