// http://www.coppelia.io/2014/07/an-a-to-z-of-extra-features-for-the-d3-force-layout/

function plotGraph(links) {
    //console.log(links);

    // Named array, node name is the key
    var nodes = {};

    // Default width and height of SVG
    var svgWidth = window.innerWidth;
    ;
    var svgHeight = window.innerHeight;

    // Raduis of node
    var nodeRadius = 6;

    // Toggle stores whether the highlighting is on
    var toggle = 0;
    // Create an index array logging what is connected to what
    var linkedByIndex = {};

    // Reduce the opacity of all other nodes, links, and text except the current highlighted node
    var reducedOpacity = 0.1;

    // Compute the distinct nodes from the links.
    links.forEach(function (link) {
        link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
        link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
    });

    //console.log(nodes);

    // Convert named array to index array
    var nodes = Object.keys(nodes).map(function (key) {
        return nodes[key];
    });

    //console.log(nodes);

    // Append a SVG to the graph container div
    // This graph auto scales with the window resize
    var svg = d3.select("#causal-graph")
            .append("svg")
            .attr("width", svgWidth)
            .attr("height", svgHeight);

    // Creates a new simulation with nodes array
    var simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink().id(function (d) {
                return d.index;
            }))
            .force("charge", d3.forceManyBody())
            .force("collide", d3.forceCollide(2)) // nodes shouldn't overlap
            .force("center", d3.forceCenter(svgWidth / 2, svgHeight / 2))
            .on("tick", ticked);

    // Can't be chained with the above call
    simulation.force("link")
            .links(links);

    // Default distance accessor is 30
    simulation.force("link")
            .distance(60);

    // Each node conects to itself, so we can highlight this node when it's selected
    for (i = 0; i < nodes.length; i++) {
        linkedByIndex[i + "," + i] = 1;
    }
    ;

    // Add all the connections based on links
    // Must call this after  simulation.force("link").links(links)
    // since the the zero-based index of each link is assigned by this method
    links.forEach(function (d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

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
            .attr("cx", 4)
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
            .attr("r", nodeRadius)
            .on('dblclick', highlightConnectedNodes)
            .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended));

    // This function looks up whether a pair are neighbours
    function neighboring(a, b) {
        return linkedByIndex[a.index + "," + b.index];
    }

    // Highlight connected nodes on double click
    function highlightConnectedNodes() {
        if (toggle == 0) {
            // Reduce the opacity of all but the neighbouring nodes/text
            d = d3.select(this).node().__data__;
            node.style("opacity", function (o) {
                return neighboring(d, o) | neighboring(o, d) ? 1 : reducedOpacity;
            });

            text.style("opacity", function (o) {
                return neighboring(d, o) | neighboring(o, d) ? 1 : reducedOpacity;
            });

            link.style("opacity", function (o) {
                return d.index == o.source.index | d.index == o.target.index ? 1 : reducedOpacity;
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
    function dragstarted() {
        if (!d3.event.active)
            simulation.alphaTarget(0.3).restart();
        d3.event.subject.fx = d3.event.subject.x;
        d3.event.subject.fy = d3.event.subject.y;
    }

    function dragged() {
        d3.event.subject.fx = d3.event.x;
        d3.event.subject.fy = d3.event.y;
    }

    function dragended() {
        if (!d3.event.active)
            simulation.alphaTarget(0);
        d3.event.subject.fx = null;
        d3.event.subject.fy = null;
    }

    // Draw node text
    var text = svg.append("g")
            .selectAll(".node-name")
            .data(nodes)
            .enter().append("text")
            .attr("class", "node-name")
            .attr("x", 8)
            .attr("y", ".31em")
            .text(function (d) {
                return d.name;
            });

    // The tick handler is the function that enables you to get the state of the layout 
    // when it has changed (the simulation has advanced by a tick) and act on it.
    // In particular, redraw the nodes and links where they currently are in the simulation.
    function ticked() {
        // Position links
        link.attr("x1", function (d) {
            return d.source.x;
        })
                .attr("y1", function (d) {
                    return d.source.y;
                })
                .attr("x2", function (d) {
                    return d.target.x;
                })
                .attr("y2", function (d) {
                    return d.target.y;
                });

        // Position nodes
        node.attr("cx", function (d) {
                // This makes sure the nodes won't go out of the container
                // Bounding box example: http://mbostock.github.io/d3/talk/20110921/bounding.html
                return d.x = Math.max(nodeRadius, Math.min(svgWidth - nodeRadius, d.x));
                //return d.x;
            })
            .attr("cy", function (d) {
                return d.y = Math.max(nodeRadius, Math.min(svgHeight - nodeRadius, d.y));
                //return d.y;
            });

        // Position node text
        text.attr("x", function (d) {
                return d.x + nodeRadius;
            })
            .attr("y", function (d) {
                return d.y + nodeRadius/2;
            });
    }

    // When the graph is huge it's nice to have some search functionality
    // This highlights the target node
    $('#searchBtn').click(function () {
        //find the node
        var selectedVal = $('#search').val();
        var node = svg.selectAll(".node");
        if (selectedVal == '') {
            node.style("stroke", "white").style("stroke-width", "1");
        } else {
            var selected = node.filter(function (d, i) {
                return d.name !== selectedVal;
            });
            selected.style("opacity", "0");
            var link = svg.selectAll(".link")
            link.style("opacity", "0");
            d3.selectAll(".node, .link").transition()
                    .duration(5000)
                    .style("opacity", 1);
        }
    });

}


