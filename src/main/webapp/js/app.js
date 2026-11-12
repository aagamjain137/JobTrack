/**
 * JobTrack Application Frontend Scripts
 * Provides delete confirmations, client-side validation, and alert dismissal.
 */
document.addEventListener("DOMContentLoaded", function () {

  // 1. Auto-dismiss flash alert banners after 5 seconds
  const alerts = document.querySelectorAll(".alert");
  alerts.forEach(function (alert) {
    setTimeout(function () {
      alert.style.transition = "opacity 0.5s ease, transform 0.5s ease";
      alert.style.opacity = "0";
      alert.style.transform = "translateY(-6px)";
      setTimeout(function () {
        alert.remove();
      }, 500);
    }, 5000);
  });

  // 2. Attach confirmation prompt to elements with data-confirm attribute
  const confirmElements = document.querySelectorAll("[data-confirm]");
  confirmElements.forEach(function (element) {
    element.addEventListener("click", function (event) {
      const message = this.getAttribute("data-confirm") || "Are you sure you want to proceed?";
      if (!window.confirm(message)) {
        event.preventDefault();
        event.stopPropagation();
        return false;
      }
    });
  });

  // 3. Client-side registration form validation (password match & complexity)
  const registerForm = document.getElementById("registerForm");
  if (registerForm) {
    registerForm.addEventListener("submit", function (event) {
      const password = document.getElementById("password");
      const confirmPassword = document.getElementById("confirmPassword");
      const name = document.getElementById("name");
      const email = document.getElementById("email");

      if (name && name.value.trim().length < 2) {
        alert("Please enter a valid full name (at least 2 characters).");
        name.focus();
        event.preventDefault();
        return false;
      }

      if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) {
        alert("Please enter a valid email address.");
        email.focus();
        event.preventDefault();
        return false;
      }

      if (password && confirmPassword) {
        if (password.value !== confirmPassword.value) {
          alert("Passwords do not match. Please verify and try again.");
          confirmPassword.focus();
          event.preventDefault();
          return false;
        }

        if (password.value.length < 8) {
          alert("Password must be at least 8 characters long.");
          password.focus();
          event.preventDefault();
          return false;
        }
      }
    });
  }

  // 4. Job application form validation (salary positive decimal check)
  const applicationForm = document.getElementById("applicationForm");
  if (applicationForm) {
    applicationForm.addEventListener("submit", function (event) {
      const company = document.getElementById("companyName");
      const role = document.getElementById("jobRole");
      const salary = document.getElementById("salary");

      if (company && company.value.trim() === "") {
        alert("Company Name is required.");
        company.focus();
        event.preventDefault();
        return false;
      }

      if (role && role.value.trim() === "") {
        alert("Job Role is required.");
        role.focus();
        event.preventDefault();
        return false;
      }

      if (salary && salary.value.trim() !== "") {
        const val = parseFloat(salary.value.trim());
        if (isNaN(val) || val <= 0) {
          alert("Salary must be a positive numeric value.");
          salary.focus();
          event.preventDefault();
          return false;
        }
      }
    });
  }

});
