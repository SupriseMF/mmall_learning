<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<html>
<body>
<h2>Hello World!</h2>

<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="通过SpringMVC上传文件"/>
</form>


<form name="form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本图片文件上传"/>
</form>

</body>
</html>
