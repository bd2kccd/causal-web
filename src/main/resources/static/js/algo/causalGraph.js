function plotGraph(links) {
    var nodes = {};

    // Compute the distinct nodes from the links.
    links.forEach(function (link) {
        link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
        link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
    });
	
console.log(nodes);

// Convert to array
var nodes = Object.keys(nodes).map(function (key) { 
	return nodes[key]; 
});

console.log(nodes);

console.log(links);

    var graphContainer = d3.select("#causal-graph");
	var graphWidth = window.innerWidth;
	var graphHeight = window.innerHeight;
	
	// Append a SVG to the body of the html page. Assign this SVG as an object to svg
    var svg = graphContainer.append("svg")
            .attr("width", graphWidth)
            .attr("height", graphHeight);

			
	// Creates a new simulation with nodes array
    var simulation = d3.forceSimulation(nodes)
		.force("link", d3.forceLink().id(function(d) { 
			return d.index; 
		}))
		.force("charge", d3.forceManyBody())
		.force("distance", d3.forceManyBody().distanceMax(10))
		.force("collide", d3.forceCollide(2)) // nodes shouldn't overlap
		.force("center", d3.forceCenter(graphWidth/2, graphHeight/2))
		.on("tick", ticked);

	// Can't be chained with the above call
    simulation.force("link")
		.links(links);
		
	// <- marker starts with arrow
    svg.append("defs").append("marker")
            .attr("id", "marker-start-arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", -5)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M10,-5L0,0L10,5")
			.attr("class", "marker-start-arrow");
	
	// o- marker starts with circle
    svg.append("defs").append("marker")
            .attr("id", "marker-start-circle")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", -5)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("circle")
            .attr("cx", 6)
            .attr("cy", 0)
            .attr("r", 4)
			.attr("class", "marker-start-circle");
	
    // -> marker ends with arrow
    svg.append("defs").append("marker")
            .attr("id", "marker-end-arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 20)
            .attr("refY", 0)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
			.attr("class", "marker-end-arrow");

	// -o marker ends with circle
    svg.append("defs").append("marker")
            .attr("id", "marker-end-circle")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("circle")
            .attr("cx", 4)
            .attr("cy", 0)
            .attr("r", 4)
			.attr("class", "marker-end-circle");

	
			
    // Add the edge based on type: link line and the arrow/circle
    var link = svg.append("g").selectAll(".link")
            .data(links)
            .enter().append("line")
            .attr("class", "link")
            .attr("marker-start", function (d) {
                if (d.type === "<->") {
                    return "url(#marker-start-arrow)";
                } else if (d.type === "o-o" || d.type === "o->") {
                    return "url(#marker-start-circle)";
                } else {
                    return "";
                }
            })
            .attr("marker-end", function (d) {
                if (d.type === "-->" || d.type === "<->" || d.type === "o->") {
                    return "url(#marker-end-arrow)";
                } else if (d.type === "o-o") {
                    return "url(#marker-end-circle)";
                } else {
                    return "";
                }
            });

	// Draw all nodes as circles
    var node = svg.append("g")
            .selectAll(".node")
            .data(nodes)
            .enter().append("circle")
			.attr("class", "node")
            .attr("r", 6)
			.on('dblclick', highlightConnectedNodes)
			.call(d3.drag()
				.on("start", dragstart)
				.on("drag", dragmove)
				.on("end", dragend));

	// Toggle stores whether the highlighting is on
	var toggle = 0;
	// Create an array logging what is connected to what
	var linkedByIndex = {};
	for (i = 0; i < nodes.length; i++) {
		linkedByIndex[i + "," + i] = 1;
	};

	links.forEach(function(d) {
		linkedByIndex[d.source.index + "," + d.target.index] = 1;
	});
	
	// This function looks up whether a pair are neighbours
	function neighboring(a, b) {
		return linkedByIndex[a.index + "," + b.index];
	}
	
	// Highlight connected nodes on double click
	function highlightConnectedNodes() {
		if (toggle == 0) {
			// Reduce the opacity of all but the neighbouring nodes/text
			d = d3.select(this).node().__data__;
			node.style("opacity", function(o) {
				return neighboring(d, o) | neighboring(o, d) ? 1 : 0.2;
			});
			
			text.style("opacity", function(o) {
				return neighboring(d, o) | neighboring(o, d) ? 1 : 0.2;
			});
			
			link.style("opacity", function(o) {
				return d.index == o.source.index | d.index == o.target.index ? 1 : 0.2;
			});
			
			// Set toggle flag
			toggle = 1;
		} else {
			// Reset opacity and toggle flag
			node.style("opacity", 1);
			text.style("opacity", 1);
			link.style("opacity", 1);
			toggle = 0;
		}
	}
	
	// Drag
	function dragstart(d, i) {
        simulation.stop() // stops the force auto positioning before you start dragging
    }
	
    function dragmove(d, i) {
		d.fx = d3.event.x;
		d.fy = d3.event.y;
    }
	
    function dragend(d, i) {
        //d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
        simulation.restart();
    }
	
	// Draw node text
    var text = svg.append("g")
            .selectAll(".node-name")
            .data(nodes)
            .enter().append("text")
			.attr("class", "node-name")
            .attr("x", 8)
            .attr("y", ".31em")
            .text(function(d) {
                return d.name;
            });

    // The tick handler is the function that enables you to get the state of the layout 
	// when it has changed (the simulation has advanced by a tick) and act on it.
	// In particular, redraw the nodes and links where they currently are in the simulation.
    function ticked() {
        // Position links
		link.attr("x1", function(d) {
				return d.source.x;
			})
			.attr("y1", function(d) {
				return d.source.y;
			})
			.attr("x2", function(d) {
				return d.target.x;
			})
			.attr("y2", function(d) {
				return d.target.y;
			});
		
		// Position nodes
        node.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
		
		// Position node text
        text.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
    }

}


