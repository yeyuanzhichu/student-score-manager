<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${mode eq 'create' ? '新增课程' : '编辑课程'}"/>
<c:set var="pageSubtitle" value="${mode eq 'create' ? '创建课程档案' : '修改课程信息'}"/>
<c:set var="activeNav" value="/teacher/courses"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="card">
  <div class="card-header">
    <div><h3>${mode eq 'create' ? '新增课程' : '编辑课程'}</h3><p class="sub">带 <span style="color:#dc2626">*</span> 为必填项</p></div>
    <a class="btn btn-sm btn-secondary" href="${ctx}/teacher/courses">
      <svg class="icon"><use href="#icon-arrow-left"/></svg>返回列表
    </a>
  </div>
  <div class="card-body">
    <c:if test="${not empty error}"><div class="form-error"><c:out value="${error}"/></div></c:if>
    <form class="form" method="post" action="${ctx}/teacher/course">
      <c:if test="${mode eq 'edit'}"><input type="hidden" name="id" value="${course.id}"></c:if>
      <div class="form-grid">
        <div class="form-group">
          <label class="form-label" for="courseCode">课程编号 <span class="req">*</span></label>
          <input class="form-control" id="courseCode" name="courseCode" type="text"
                 value="<c:out value='${course.courseCode}'/>" placeholder="如 CS401" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="courseName">课程名称 <span class="req">*</span></label>
          <input class="form-control" id="courseName" name="courseName" type="text"
                 value="<c:out value='${course.courseName}'/>" placeholder="如 操作系统" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="credit">学分 <span class="req">*</span></label>
          <input class="form-control" id="credit" name="credit" type="number" min="0.5" max="20" step="0.5"
                 value="<c:out value='${course.credit}'/>" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="teacherName">任课教师 <span class="req">*</span></label>
          <input class="form-control" id="teacherName" name="teacherName" type="text"
                 value="<c:out value='${course.teacherName}'/>" placeholder="教师姓名" required>
        </div>
        <div class="form-group full">
          <label class="form-label" for="semester">学期 <span class="req">*</span></label>
          <input class="form-control" id="semester" name="semester" type="text"
                 value="<c:out value='${course.semester}'/>" placeholder="如 2026-2027-1" required>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn btn-primary" type="submit">
          <svg class="icon"><use href="#icon-plus"/></svg>${mode eq 'create' ? '保存课程' : '保存修改'}
        </button>
        <a class="btn btn-secondary" href="${ctx}/teacher/courses">取消</a>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
