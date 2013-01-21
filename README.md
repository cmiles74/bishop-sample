This project provides a sample application that uses the
[Bishop library](https://github.com/cmiles74/bishop). Bishop makes
it much easier to provide an API for your web-service that treats HTTP
as a first-class application layer. This sample application provides a
simple web-service that lets you manage a to-do list.

## Building the Application

You can check-out the project and the build it.

```
git clone git://github.com/tnr-global/bishop-sample.git
cd bishop-sample
lein uberjar
...watch Leiningen build...
java -jar bishop-sample-1.0-standalone.jar
```

## How it Works

This application provides a web-service that manages a to-do
list. This web-service speaks JSON, you can retrieve the list of items
with a GET request or add new items with a POST. Existing to-do items
are easily updated by making a PUT request to the to-do item's URL
with new data.

In addition to JSON web-service, this application provides a
bare-bones web application that lets you view the list of to-do items
as well as detail on each item. Once you have the application
up-and-running, you can view the list at...

[http://localhost:3000/todos](http://localhost:3000/todos)

The Bishop library handles content negotiation on behalf of the
application. It compares the headers provided by the client web
browser against the media types provided by the resource and picks the
one that matches.

Bishop also helps you write a web-service that has predictable caching
behavior. If you select a to-do item in your web-browser, you'll
notice that the first request returns a 200 response with the details
on that item. Subsequent re-loading of the page will cause "304 Not
Modified" responses to be sent to the client, causing the page to be
served out of cache. In this example the client is providing the
"ETag" of the to-do item and Bishop compares this against the current
value of the to-do item, since the to-do item hasn't changed they
match and the page can be safely served from cache.

Bishop makes creating a web-service that treats HTTP as a first-class
application protocol a straightforward and painless process. Take a
look at the
[sample code](https://github.com/cmiles74/bishop-sample/blob/master/src/com/tnrglobal/bishopsample/service.clj),
we provide a handful of callback functions and the library handles the
rest.

## Sample Transactions

The sample application provides a web service will respond to the
standard create, update and delete requests. Examples of these
requests are detailed below.

### Listing All To-Do Items

```
$ curl -i http://localhost:3000/todos

HTTP/1.1 200 OK
Date: Thu, 13 Sep 2012 13:24:08 GMT
Vary: accept-encoding, accept-charset, accept
Last-Modified: Thu, 13 Sep 2012 13:24:03 GMT
Content-Type: application/json; charset=utf-8
Content-Length: 142
Server: Jetty(7.6.1.v20120215)

[
    {
        "_links": {
            "self": "todos/1"
        },
        "created": "2012-09-13T09:24:03.324-04:00",
        "description": "Write more to-dos!",
        "id": 1,
        "title": "Your first to-do"
    }
]
```

### Updating a To-Do Item

```
$ curl -i http://localhost:3000/todos/1 -H 'Content-Type: application/json' \
-XPUT -d '{"title" : "More creative writing", \
"description" : "Write more blog posts"}'

HTTP/1.1 200 OK
Date: Thu, 13 Sep 2012 13:25:02 GMT
Vary: accept-encoding, accept-charset, accept
ETag: "1347542643324-1"
Last-Modified: Thu, 13 Sep 2012 13:24:03 GMT
Content-Type: application/json; charset=utf-8
Content-Length: 140
Server: Jetty(7.6.1.v20120215)

{
    "_links": {
        "self": "todos/1"
    },
    "created": "2012-09-13T09:24:03.324-04:00",
    "description": "Write more to-dos!",
    "id": 1,
    "title": "Your first to-do"
}
```

### Adding A New To-Do Item

```
$ curl -i http://localhost:3000/todos -H 'Content-Type: application/json' \
-XPOST -d '{
"title" : "Work on web service",
"description" : "Work on your web service error handling"}'

HTTP/1.1 303 See Other
Date: Thu, 13 Sep 2012 13:26:54 GMT
Vary: accept-encoding, accept-charset, accept
Location: todos/2
Content-Type: application/json; charset=utf-8
Content-Length: 0
Server: Jetty(7.6.1.v20120215)
```

### Retrieving a To-Do Item

```
$ curl -i http://localhost:3000/todos/2

HTTP/1.1 200 OK
Date: Thu, 13 Sep 2012 13:27:15 GMT
Vary: accept-encoding, accept-charset, accept
ETag: "1347542814965-2"
Last-Modified: Thu, 13 Sep 2012 13:26:54 GMT
Content-Type: application/json; charset=utf-8
Content-Length: 164
Server: Jetty(7.6.1.v20120215)

{
    "_links": {
        "self": "todos/2"
    },
    "created": "2012-09-13T09:26:54.965-04:00",
    "description": "Work on your web service error handling",
    "id": 2,
    "title": "Work on web service"
}
```

### Deleting a To-Do Item

```
$ curl -i http://localhost:3000/todos/2 -XDELETE

HTTP/1.1 204 No Content
Date: Thu, 13 Sep 2012 13:27:34 GMT
Vary: accept-encoding, accept-charset, accept
Content-Type: application/json; charset=utf-8
Server: Jetty(7.6.1.v20120215)
```
