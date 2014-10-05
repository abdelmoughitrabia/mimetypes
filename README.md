# MimeTypes

Java (JVM) lookup table for standard mime types and their extensions.

# API Usage

Get the extension for a mime type:

    MimeTypes.getInstance().getByType("image/png").getExtension(); // returns "png"

Get all possible extensions for a mime type:

    MimeTypes.getInstance().getByType("image/png").getExtensions(); // returns String[]{"png"}
    MimeTypes.getInstance().getByType("text/html").getExtensions(); // returns String[]{"html", "htm"}

Get mime type for an extension:

    MimeTypes.getInstance().getByExtension("png").getMimeType(); // returns "image/png"

Register a custom mimetype and query it up later:

    MimeTypes.getInstance().register(new MimeType("application/vnd.awes", new String[]{"ome"}));
    MimeTypes.getInstance().getByType("application/vnd.awesome").getExtension(); // returns "ome"

Initialize with a custom "mime.types" definitions file:

    new MimeTypes(Paths.get("/path/to/custom/mime.types"));

Alternatively:

    MimeTypes.blank().load(Paths.get("/path/to/custom/mime.types"));

# Included mime types

`MimeTypes.getInstance()` or the empty constructor `new MimeTypes()` will load
the included `mime.types` definitions, which is downloaded from:

    http://svn.apache.org/repos/asf/httpd/httpd/trunk/docs/conf/mime.types

# License

* [MIT License](http://www.opensource.org/licenses/mit-license.php)
