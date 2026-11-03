<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="成绩录入"/>
<c:set var="pageSubtitle" value="按班级和课程批量录入或修改成绩"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<c:choose>
<c:when test="${emptyData}">
  <div class="card"><div class="empty-state">
    <svg class="icon"><use href="#icon-clipboard"/></svg>
    <h3>暂无可录入数据</h3><p>请先在「课程管理」中添加课程、在「学生管理」中添加学生。</p>
  </div></div>
</c:when>
<c:otherwise>
  <form class="card" method="get" action="${ctx}/teacher/scores">
    <div class="filter-bar">
      <div class="form-group">
        <label class="form-label" for="classId">班级</label>
        <select class="form-select" id="classId" name="classId">
          <c:forEach var="c" items="${classes}">
            <option value="${c.id}" ${c.id eq selectedClassId ? 'selected' : ''}><c:out value="${c.className}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label" for="courseId">课程</label>
        <select class="form-select" id="courseId" name="courseId">
          <c:forEach var="co" items="${courses}">
            <option value="${co.id}" ${co.id eq selectedCourseId ? 'selected' : ''}><c:out value="${co.courseName}"/></option>
          </c:forEach>
        </select>
      </div>
      <button class="btn btn-primary" type="submit">
        <svg class="icon"><use href="#icon-search"/></svg>筛选
      </button>
      <a class="btn btn-secondary" href="${ctx}/teacher/export?classId=${selectedClassId}&courseId=${selectedCourseId}">
        <svg class="icon"><use href="#icon-download"/></svg>导出本课成绩
      </a>
    </div>
  </form>

  <div class="card">
    <div class="card-header">
      <div><h3><c:out value="${selectedClass.className}"/> · <c:out value="${selectedCourse.courseName}"/></h3>
        <p class="sub">共 ${rosterStats.total} 名学生，已录入 ${rosterStats.entered} 条，待录入 ${rosterStats.pending} 条</p>
      </div>
      <div style="display:flex;gap:12px">
        <span class="badge badge-info">平均分 <c:choose><c:when test="${not empty rosterStats.averageScore}"><fmt:formatNumber value="${rosterStats.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></span>
        <span class="badge badge-good">及格率 <c:choose><c:when test="${not empty rosterStats.passRate}"><fmt:formatNumber value="${rosterStats.passRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></span>
      </div>
    </div>

    <form method="post" action="${ctx}/teacher/scores">
      <input type="hidden" name="classId" value="${selectedClassId}">
      <input type="hidden" name="courseId" value="${selectedCourseId}">
      <div class="card-body no-pad">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th style="width:56px">序号</th><th>学号</th><th>姓名</th><th>性别</th><th style="width:130px">成绩</th><th>状态</th><th>更新时间</th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${roster}" varStatus="st">
              <tr>
                <td class="muted">${st.index + 1}</td>
                <td class="strong"><c:out value="${r.studentNo}"/></td>
                <td><c:out value="${r.name}"/></td>
                <td class="muted"><c:out value="${r.gender}"/></td>
                <td>
                  <input class="score-input" type="number" name="score_${r.studentId}"
                         value="<c:out value='${r.score}'/>" min="0" max="100" step="0.5"
                         placeholder="未录入" title="0~100，留空表示清除该成绩">
                </td>
                <td>
                  <c:choose>
                    <c:when test="${empty r.score}"><span class="badge badge-muted">待录入</span></c:when>
                    <c:when test="${r.score ge 90}"><span class="badge badge-excellent">优秀</span></c:when>
                    <c:when test="${r.score ge 80}"><span class="badge badge-good">良好</span></c:when>
                    <c:when test="${r.score ge 70}"><span class="badge badge-fair">中等</span></c:when>
                    <c:when test="${r.score ge 60}"><span class="badge badge-pass">及格</span></c:when>
                    <c:otherwise><span class="badge badge-fail">不及格</span></c:otherwise>
                  </c:choose>
                </td>
                <td class="muted"><c:choose><c:when test="${not empty r.updatedAt}"><fmt:formatDate value="${r.updatedAt}" pattern="yyyy-MM-dd HH:mm"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
              </tr>
            </c:forEach>
            <c:if test="${empty roster}">
              <tr><td colspan="7" class="muted" style="text-align:center;padding:28px">该班级暂无学生</td></tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card-body" style="display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap">
        <p class="form-hint" style="margin:0">提示：留空并保存将清除该生该课程成绩；成绩范围为 0～100。</p>
        <button class="btn btn-primary" type="submit" data-confirm="确认保存当前班级的 ${selectedCourse.courseName} 成绩？">
          <svg class="icon"><use href="#icon-scores"/></svg>保存全部成绩
        </button>
      </div>
    </form>
  </div>
</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
