var lunrIndex,
    results,
    pagesIndex;

// Initialize lunrjs using our generated index file
function initLunr() {
    // First retrieve the index file
    $.getJSON("js/lunr/index.json")
        .done(function(index) {
            pagesIndex = index;
            console.log("index:", pagesIndex);

            // Set up lunrjs by declaring the fields we use
            // Also provide their boost level for the ranking
            lunrIndex = lunr(function() {
                /* this.field("title", {boost: 10});
                 * this.field("tags", {boost: 5}); */
                this.field("title");
                this.field("tags");
                this.field("content");

                // ref is the result item identifier (I chose the page URL)
                this.ref("uri");

                // Feed lunr with each file and let lunr actually index them
                pagesIndex.forEach(function(page) {
                    this.add(page);
                }, this)
            });
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.error("Error getting Hugo index flie:", err);
        });
}

// Nothing crazy here, just hook up a listener on the input field
function initUI() {
    results = $("#results");
    console.log("initUI:", results);
    $("#search").keyup(function() {
        results.empty();

        // Only trigger a search when 2 chars. at least have been provided
        var query = $(this).val();
        if (query.length < 2) {
            return;
        }

        var resp = search(query);

        renderResults(resp);
    });
}

/**
 * Trigger a search in lunr and transform the result
 *
 * @param  {String} query
 * @return {Array}  results
 */
function search(query) {
    // Find the item in our index corresponding to the lunr one to have more info
    // Lunr result:
    //  {ref: "/section/page1", score: 0.2725657778206127}
    // Our result:
    //  {title:"Page1", href:"/section/page1", ...}

    console.log("search: query", query);
    console.log("search: lunrIndex", lunrIndex);
    var search_results = lunrIndex.search(query);

    console.log("search: search_results", search_results);

    return search_results.map(
        function(result) {
            console.log("search: map result", result);

            var filtered = pagesIndex.filter(
                function(page) {
                    return page.uri === result.ref;
                }
            );
            console.log("search: filtered", filtered);
            return filtered[0];
        });
}

/**
 * Display the 10 first results
 *
 * @param  {Array} results to display
 */
function renderResults(resp) {
    if (!resp.length) {
        results.append("No results");
    }

    const max_result = 17;

    resp.slice(0, max_result).forEach(function(result) {
        let newItem = document.createElement("li");
        let newLink = document.createElement("a");
        newLink.textContent = result.title;
        newLink.href = result.uri;
        newItem.appendChild(newLink);
        results.append(newItem);
    });

    if (max_result < resp.length) {
        let newItem = document.createElement("i");
        let newLink = document.createElement("a");
        newLink.textContent = "Show more";
        newLink.href = "/search";
        newItem.appendChild(newLink);
        results.append(newItem);
    }
}

// Let's get started
initLunr();

$(document).ready(function() {
    initUI();
});

