<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="path" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>CCD Demo: D3 Graph</title>
        <style>
            .link {
                fill: none;
                stroke: #666;
                stroke-width: 1.5px;
            }
            circle {
                fill: #ccc;
                stroke: #333;
                stroke-width: 1.5px;
            }
            text {
                font: 10px sans-serif;
                pointer-events: none;
                text-shadow: 0 1px 0 #fff, 1px 0 0 #fff, 0 -1px 0 #fff, -1px 0 0 #fff;
            }
        </style>
    </head>
    <body>
        <script src="${path}/vendor/d3.v3.min.js"></script>
        <script>
            var links = ${data};
            var nodes = {};

            // Compute the distinct nodes from the links.
            links.forEach(function (link) {
                link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
                link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
            });

            var width = window.innerWidth;
            var height = window.innerHeight;

            var svg = d3.select("body").append("svg")
                    .attr("width", "100%")
                    .attr("height", height);

            var force = d3.layout.force()
                    .nodes(d3.values(nodes))
                    .links(links)
                    .size([width, height])
                    .linkDistance(60)
                    .charge(-300)
                    .on("tick", tick)
                    .start();

            // create the arrows
            svg.append("svg:defs").append("svg:marker")
                    .attr("id", "end-arrow")
                    .attr("viewBox", "0 -5 10 10")
                    .attr("refX", 15)
                    .attr("refY", -1.5)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 6)
                    .attr("orient", "auto")
                    .append("svg:path")
                    .attr("d", "M0,-5L10,0L0,5");

            svg.append("svg:defs").append("svg:marker")
                    .attr("id", "start-arrow")
                    .attr("viewBox", "0 -5 10 10")
                    .attr("refX", -5)
                    .attr("refY", -1.5)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 6)
                    .attr("orient", "auto")
                    .append("svg:path")
                    .attr("d", "M10,-5L0,0L10,5");

            svg.append("svg:defs").append("svg:marker")
                    .attr("id", "circle-start-arrow")
                    .attr("viewBox", "0 -5 10 10")
                    .attr("refX", -5)
                    .attr("refY", -1.5)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 6)
                    .attr("orient", "auto")
                    .append("svg:circle")
                    .attr("cx", 6)
                    .attr("cy", 0)
                    .attr("r", 4);

            svg.append("svg:defs").append("svg:marker")
                    .attr("id", "circle-end-arrow")
                    .attr("viewBox", "0 -5 10 10")
                    .attr("refX", 15)
                    .attr("refY", -1.5)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 6)
                    .attr("orient", "auto")
                    .append("svg:circle")
                    .attr("cx", 4)
                    .attr("cy", 0)
                    .attr("r", 4);

            // add the links and the arrows
            var path = svg.append("svg:g").selectAll("path")
                    .data(force.links())
                    .enter().append("svg:path")
                    .attr("class", "link")
                    .attr("marker-start", function (d) {
                        if (d.type === "<->") {
                            return "url(#start-arrow)";
                        } else if (d.type === "o-o" || d.type === "o->") {
                            return "url(#circle-start-arrow)";
                        } else {
                            return "";
                        }
                    })
                    .attr("marker-end", function (d) {
                        if (d.type === "-->" || d.type === "<->" || d.type === "o->") {
                            return "url(#end-arrow)";
                        }  else if (d.type === "o-o") {
                            return "url(#circle-end-arrow)";
                        } else {
                            return "";
                        }
                    });

            var circle = svg.append("svg:g")
                    .selectAll("circle")
                    .data(force.nodes())
                    .enter().append("svg:circle")
                    .attr("r", 6)
                    .call(force.drag);

            var text = svg.append("svg:g")
                    .selectAll("text")
                    .data(force.nodes())
                    .enter().append("svg:text")
                    .attr("x", 8)
                    .attr("y", ".31em")
                    .text(function (d) {
                        return d.name;
                    });

            // Use elliptical arc path segments to doubly-encode directionality.
            function tick() {
                path.attr("d", linkArc).each(function () {
                    this.parentNode.insertBefore(this, this);
                });
                circle.attr("transform", transform);
                text.attr("transform", transform);
            }

            function linkArc(d) {
                var dx = d.target.x - d.source.x,
                        dy = d.target.y - d.source.y,
                        dr = Math.sqrt(dx * dx + dy * dy);
                return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
            }

            function transform(d) {
                return "translate(" + d.x + "," + d.y + ")";
            }
        </script>
    </body>
</html>
