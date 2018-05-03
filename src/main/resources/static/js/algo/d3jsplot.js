// http://www.coppelia.io/2014/07/an-a-to-z-of-extra-features-for-the-d3-force-layout/

function plotGraph(links) {
    //console.log(links);

    // Named array, node name is the key
    var nodes = {};

    // Default width and height of SVG
    // -40 so we don't see the scrollbars
    var svgWidth = window.innerWidth - 40;
    var svgHeight = window.innerHeight - 40;

    // Raduis of node
    var nodeRadius = 6;

    // Toggle stores whether the highlighting is on
    var toggle = 0;
    // Create an index array logging what is connected to what
    var linkedByIndex = {};

    // Reduce the opacity of all other nodes, links, and text except the current highlighted node
    var reducedOpacity = 0.1;

    // Define the div for the tooltip
    var div = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);

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

    // Must defien this before the var svg
    var zoom = d3.zoom()
            .scaleExtent([.2, 10]) // // zoom scale x.2 to x10
            .on("zoom", zoomed);

    // Append a SVG to the graph container div
    // This graph auto scales with the window resize
    var svg = d3.select("#causal-graph")
            .append("svg")
            .attr("width", svgWidth)
            .attr("height", svgHeight)
            .call(zoom)
            .on("dblclick.zoom", null); // This disables zoom in behavior caused by double click

    // This graphGroup groups all graph elements
    var graphGroup = svg.append("g");

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
            .distance(50);

    // Each node conects to itself, so we can highlight this node when it's selected
    for (i = 0; i < nodes.length; i++) {
        linkedByIndex[i + "," + i] = 1;
    }

    // Add all the connections based on links
    // Must call this after  simulation.force("link").links(links)
    // since the the zero-based index of each link is assigned by this method
    links.forEach(function (d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

    // <- marker starts with arrow
    graphGroup.append("defs").append("marker")
            .attr("id", "marker-start-arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", -10)
            .attr("refY", 0)
            .attr("markerWidth", nodeRadius)
            .attr("markerHeight", nodeRadius)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M10,-5L0,0L10,5")
            .attr("class", "marker-start-arrow");

    // o- marker starts with circle
    graphGroup.append("defs").append("marker")
            .attr("id", "marker-start-circle")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", -8)
            .attr("refY", 0)
            .attr("markerWidth", nodeRadius)
            .attr("markerHeight", nodeRadius)
            .attr("orient", "auto")
            .append("circle")
            .attr("cx", 6)
            .attr("cy", 0)
            .attr("r", 4)
            .attr("class", "marker-start-circle");

    // -> marker ends with arrow
    graphGroup.append("defs").append("marker")
            .attr("id", "marker-end-arrow")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 20)
            .attr("refY", 0)
            .attr("markerWidth", nodeRadius)
            .attr("markerHeight", nodeRadius)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr("class", "marker-end-arrow");

    // -o marker ends with circle
    graphGroup.append("defs").append("marker")
            .attr("id", "marker-end-circle")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 20)
            .attr("refY", 0)
            .attr("markerWidth", nodeRadius)
            .attr("markerHeight", nodeRadius)
            .attr("orient", "auto")
            .append("circle")
            .attr("cx", 6)
            .attr("cy", 0)
            .attr("r", 4)
            .attr("class", "marker-end-circle");

    // Add the edge based on type: link line and the arrow/circle
    var link = graphGroup.append("g").selectAll(".link")
            .data(links)
            .enter().append("path") // don't use line
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
            })
            .style('stroke', function (d) {
                if (d.edgeProps !== null && d.edgeProps.indexOf('dd') >= 0) {
                    return d3.rgb(0, 128, 0);
                }
            })
            .style("stroke-dasharray", function (d) {
                if (d.edgeProps !== null && d.edgeProps.indexOf('nl') >= 0) {
                    return (6, 3);
                }
            })
            .on("mouseover", function (d) {
                if (d.bootstrap !== null) {
                    var bootstrapString = "";
                    d.bootstrap.forEach(function(item) {
                        bootstrapString += item;
                    });

                    div.transition()
                            .duration(200)
                            .style("opacity", .9);
                    div.html(bootstrapString)
                            .style("left", (d3.event.pageX) + "px")
                            .style("top", (d3.event.pageY - 28) + "px");
                }
            })
            .on("mouseout", function (d) {
                if (d.bootstrap !== null) {
                    div.transition()
                            .duration(500)
                            .style("opacity", 0);
                }
            });

    // Draw all nodes as circles
    // In order to cover the links, this code must be after drawing links
    var node = graphGroup.append("g")
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

    // Draw node text
    var text = graphGroup.append("g")
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
        // Position nodes
        node.attr("cx", function (d) {
            return d.x;
        })
                .attr("cy", function (d) {
                    return d.y;
                });

        // Position links
        // Use path instead of line since IE 10 doesn't render the links correctly
        link.attr("d", positionLink).each(function () {
            this.parentNode.insertBefore(this, this);
        });

        // Position node text
        text.attr("x", function (d) {
            return d.x - (nodeRadius / 2);
        })
                .attr("y", function (d) {
                    return d.y - (1.5 * nodeRadius);
                });
    }

    // Position the edge link
    // Use path to draw a straight line, don't use line since IE bug
    function positionLink(d) {
        return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
    }

    // Zooming
    function zoomed() {
        graphGroup.attr("transform", d3.event.transform);
    }

    // This function looks up whether a pair are neighbours
    function neighboring(a, b) {
        return linkedByIndex[a.index + "," + b.index];
    }

    // Highlight connected nodes on double click
    function highlightConnectedNodes() {
        if (toggle === 0) {
            // Reduce the opacity of all but the neighbouring nodes/text
            d = d3.select(this).node().__data__;
            node.style("opacity", function (o) {
                return neighboring(d, o) | neighboring(o, d) ? 1 : reducedOpacity;
            });

            text.style("opacity", function (o) {
                return neighboring(d, o) | neighboring(o, d) ? 1 : reducedOpacity;
            });

            link.style("opacity", function (o) {
                return d.index === o.source.index | d.index === o.target.index ? 1 : reducedOpacity;
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

    // When the graph is huge it's nice to have some search functionality
    // This highlights the target node
    $('#searchBtn').click(function () {
        //find the node
        var selectedVal = $('#search').val();
        var node = graphGroup.selectAll(".node");
        if (selectedVal === '') {
            node.style("stroke", "white").style("stroke-width", "1");
        } else {
            var selected = node.filter(function (d, i) {
                // Make the search case-insensitive
                return d.name.toLowerCase() !== selectedVal.toLowerCase();
            });
            selected.style("opacity", "0");
            var link = graphGroup.selectAll(".link")
            link.style("opacity", "0");
            d3.selectAll(".node, .link").transition()
                    .duration(5000)
                    .style("opacity", 1);
        }
    });

}
