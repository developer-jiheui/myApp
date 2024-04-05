<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="contextPath" value="<%=request.getContextPath()%>"/>
<c:set var="dt" value="<%=System.currentTimeMillis()%>"/>

<jsp:include page="../layout/header.jsp"/>

<h1 class="title">블로그 목록</h1>

<!-- include libraries(jquery, bootstrap) -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
<link href="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css" rel="stylesheet">
<script src="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<!-- include summernote css/js -->
<link rel="stylesheet" href="${contextPath}/resources/summernote-0.8.18-dist/summernote.min.css">
<script src="${contextPath}/resources/summernote-0.8.18-dist/summernote.min.js"></script>
<script src="${contextPath}/resources/summernote-0.8.18-dist/lang/summernote-ko-KR.min.js"></script>

</head>
<body>

<form method="POST"
        action="${contextPath}/board/register.do">
    <div>
      <textarea id="contents" name="contents"></textarea>
    </div>
    <div>
      <button type="submit">전송</button>
    </div>
  </form>

  <script>
    $(document).ready(function(){
      $('#contents').summernote({
        width: 1024,
        height: 500,
        lang: 'ko-KR',
        callbacks: {
          onImageUpload: (images)=>{
            // 비동기 방식을 이용한 이미지 업로드
            for(let i = 0; i < images.length; i++) {
              let formData = new FormData();
              formData.append('image', images[i]);
              fetch('${contextPath}/summernote/imageUpload.do', {
                method: 'POST',
                body: formData
              }).then(response=>response.json())
                .then(resData=>{
                  $('#contents').summernote('insertImage', resData.src);
                });
            }
          }
        }
      });
    })
  </script>


<%@ include file="../layout/footer.jsp" %>
