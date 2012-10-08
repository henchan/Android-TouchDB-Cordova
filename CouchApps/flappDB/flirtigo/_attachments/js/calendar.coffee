Backbone.d3.Canned["Calendar"] =
  View: Backbone.d3.PlotView.extend(
    day: d3.time.format("%w")
    week: d3.time.format("%U")
    percent: d3.format(".1%")
    format: d3.time.format("%Y-%m-%d")
    m: [ 19, 20, 20, 19 ]
    gw: 700
    gh: 136
    z: 10
    plot: (options) ->
      that = this
      w = 700 - @m[1] - @m[3]
      h = 136 - @m[0] - @m[2]
      color = d3.scale.quantize().domain([ 0, 1 ]).range(d3.range(9))
      svg = @div.selectAll("svg").data(d3.range(1985, 2011)).enter().append("svg:svg").attr("width", w + @m[1] + @m[3]).attr("height", h + @m[0] + @m[2]).attr("class", "RdYlGn").append("svg:g").attr("transform", "translate(" + (@m[3] + (w - @z * 53) / 2) + "," + (@m[0] + (h - @z * 7) / 2) + ")")
      svg.append("svg:text").attr("transform", "translate(-6," + @z * 3.5 + ")rotate(-90)").attr("text-anchor", "middle").text String
      rect = svg.selectAll("rect.day").data((d) ->
        d3.time.days new Date(d, 0, 1), new Date(d + 1, 0, 1)
      ).enter().append("svg:rect").attr("class", "day").attr("width", @z).attr("height", @z).attr("x", (d) ->
        that.week(d) * that.z
      ).attr("y", (d) ->
        that.day(d) * that.z
      )
      svg.selectAll("path.month").data((d) ->
        d3.time.months new Date(d, 0, 1), new Date(d + 1, 0, 1)
      ).enter().append("svg:path").attr("class", "month").attr "d", that.monthPath
      data = d3.nest().key((d) ->
        d.date
      ).rollup((d) ->
        d[0].count
      ).map(@plotdata())
      rect.attr("class", (d) ->
        "day q" + color(data[that.format(d)]) + "-9"
      ).append("svg:title").text (d) ->
        (d = that.format(d)) + (if d of data then ": " + that.percent(data[d]) else "")

    plotdata: ->
      data = []
      max = @collection.max((d) ->
        d.get "count"
      ).get("count")
      @collection.forEach (datapoint) ->
        data.push
          date: datapoint.get("date")
          count: 1 - parseFloat(datapoint.get("count")) / max

      data

    monthPath: (t0) ->
      t1 = new Date(t0.getUTCFullYear(), t0.getUTCMonth() + 1, 0)
      d0 = +@day(t0)
      w0 = +@week(t0)
      d1 = +@day(t1)
      w1 = +@week(t1)
      "M" + (w0 + 1) * @z + "," + d0 * @z + "H" + w0 * @z + "V" + 7 * @z + "H" + w1 * @z + "V" + (d1 + 1) * @z + "H" + (w1 + 1) * @z + "V" + 0 + "H" + (w0 + 1) * @z + "Z"
  )
  Model: Backbone.Model.extend(initialize: (data) ->
    @set
      date: data.date
      count: data.count
    _db: Backbone.couch.db 'def_cult'
    defaults:
      type: 'calendar'
  )
  Collection: Backbone.d3.couch.PlotCollection.extend(
    _db: Backbone.couch.db 'def_cult'
    change_feed: true
    couch: () ->
      view: 'flirtigo/type'
      key: 'crashes'
      limit: 100
      include_docs: true  
    
    model: @Model
    success: (result, foo) ->
      models = []
      _.each result, (row) ->
        models.push new CalDataPoint(row.value)

      models = null  if models.length is 0
      models
  )