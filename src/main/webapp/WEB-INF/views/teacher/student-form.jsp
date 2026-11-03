<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${mode eq 'create' ? '新增学生' : '编辑学生'}"/>
<c:set var="pageSubtitle" value="${mode eq 'create' ? '创建学生档案并生成登录账号' : '修改学生档案信息'}"/>
<c:set var="activeNav" value="/teacher/students"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="card">
  <div class="card-header">
    <div><h3>${mode eq 'create' ? '新增学生' : '编辑学生'}</h3><p class="sub">带 <span style="color:#dc2626">*</span> 为必填项</p></div>
    <a class="btn btn-sm btn-secondary" href="${ctx}/teacher/students">
      <svg class="icon"><use href="#icon-arrow-left"/></svg>返回列表
    </a>
  </div>
  <div class="card-body">
    <c:if test="${not empty error}"><div class="form-error"><c:out value="${error}"/></div></c:if>
    <form class="form" method="post" action="${ctx}/teacher/student">
      <c:if test="${mode eq 'edit'}"><input type="hidden" name="id" value="${student.id}"></c:if>
      <div class="form-grid">
        <div class="form-group">
          <label class="form-label" for="studentNo">学号 <span class="req">*</span></label>
          <input class="form-control" id="studentNo" name="studentNo" type="text"
                 value="<c:out value='${student.studentNo}'/>" placeholder="如 2024010101" required>
          <p class="form-hint">学生登录系统时使用的账号</p>
        </div>
        <div class="form-group">
          <label class="form-label" for="name">姓名 <span class="req">*</span></label>
          <input class="form-control" id="name" name="name" type="text"
                 value="<c:out value='${student.name}'/>" placeholder="学生姓名" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="gender">性别 <span class="req">*</span></label>
          <select class="form-select" id="gender" name="gender">
            <option value="男" ${student.gender eq '男' ? 'selected' : ''}>男</option>
            <option value="女" ${student.gender eq '女' ? 'selected' : ''}>女</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label" for="classId">班级 <span class="req">*</span></label>
          <select class="form-select" id="classId" name="classId">
            <c:forEach var="c" items="${classes}">
              <option value="${c.id}" ${c.id eq student.classId ? 'selected' : ''}><c:out value="${c.className}"/></option>
            </c:forEach>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label" for="phone">联系电话</label>
          <input class="form-control" id="phone" name="phone" type="text"
                 value="<c:out value='${student.phone}'/>" placeholder="选填">
        </div>
        <div class="form-group">
          <label class="form-label" for="password">${mode eq 'create' ? '初始密码' : '重置密码'}</label>
          <input class="form-control" id="password" name="password" type="text"
                 placeholder="${mode eq 'create' ? '默认 123456' : '留空则保持原密码'}"
                 value="${mode eq 'create' ? '123456' : ''}">
        </div>
      </div>
      <div class="form-actions">
        <button class="btn btn-primary" type="submit">
          <svg class="icon"><use href="#icon-plus"/></svg>${mode eq 'create' ? '保存学生' : '保存修改'}
        </button>
        <a class="btn btn-secondary" href="${ctx}/teacher/students">取消</a>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
