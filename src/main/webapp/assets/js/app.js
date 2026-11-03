(function () {
  'use strict';

  // 侧边栏在移动端的展开/收起
  var menuBtn = document.getElementById('menuBtn');
  var sidebar = document.getElementById('sidebar');
  var scrim = document.getElementById('sidebarScrim');
  if (menuBtn && sidebar && scrim) {
    menuBtn.addEventListener('click', function () {
      document.body.classList.toggle('sidebar-open');
    });
    scrim.addEventListener('click', function () {
      document.body.classList.remove('sidebar-open');
    });
  }

  // 提示条 5 秒后自动淡出
  document.querySelectorAll('.alert').forEach(function (alert) {
    var close = alert.querySelector('.alert-close');
    if (close) {
      close.addEventListener('click', function () {
        alert.remove();
      });
    }
    window.setTimeout(function () {
      if (alert && alert.parentNode) {
        alert.style.transition = 'opacity .4s ease, transform .4s ease';
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-6px)';
        window.setTimeout(function () { alert.remove(); }, 400);
      }
    }, 5000);
  });

  // 带 data-confirm 的表单提交前确认
  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (event) {
      if (!window.confirm(form.getAttribute('data-confirm'))) {
        event.preventDefault();
      }
    });
  });

  // 登录页演示账号一键填充
  document.querySelectorAll('.demo-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var account = document.querySelector('input[name="account"]');
      var password = document.querySelector('input[name="password"]');
      if (account) { account.value = btn.getAttribute('data-account') || ''; }
      if (password) { password.value = btn.getAttribute('data-password') || ''; }
      if (account) { account.focus(); }
    });
  });

  // 筛选下拉框变化时自动提交（仅限带 data-autosubmit 的表单）
  document.querySelectorAll('form[data-autosubmit] select').forEach(function (select) {
    select.addEventListener('change', function () {
      select.form.submit();
    });
  });

  // 成绩录入：实时校验 0-100，非法时标红
  document.querySelectorAll('.score-input').forEach(function (input) {
    input.addEventListener('input', function () {
      var value = input.value;
      if (value === '') {
        input.classList.remove('invalid');
        return;
      }
      var number = Number(value);
      if (Number.isNaN(number) || number < 0 || number > 100) {
        input.classList.add('invalid');
      } else {
        input.classList.remove('invalid');
      }
    });
  });

  // 顶栏日期显示
  var dateChip = document.getElementById('dateChip');
  if (dateChip) {
    var now = new Date();
    var week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()];
    dateChip.textContent = now.getFullYear() + '年' + (now.getMonth() + 1) + '月'
      + now.getDate() + '日 星期' + week;
  }

  // 打印成绩单
  var printBtn = document.getElementById('printBtn');
  if (printBtn) {
    printBtn.addEventListener('click', function () { window.print(); });
  }
})();
