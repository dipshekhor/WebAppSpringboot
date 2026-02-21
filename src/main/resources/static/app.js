// Global state
let currentUser = null;
let currentPassword = null;
let isAdmin = false;

const API_BASE_URL = '/api';

// Helper Functions
function fillCredentials(username, password) {
    document.getElementById('username').value = username;
    document.getElementById('password').value = password;
}

function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('passwordToggleIcon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.textContent = 'visibility_off';
    } else {
        passwordInput.type = 'password';
        toggleIcon.textContent = 'visibility';
    }
}

// Authentication
function loginWithCredentials(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showLoginError('Please enter both username and password');
        return;
    }

    login(username, password);
}

function login(username, password) {
    // Clear any previous error messages
    showLoginError('');

    // Test the credentials with the API
    fetch(`${API_BASE_URL}/teachers`, {
        headers: {
            'Authorization': 'Basic ' + btoa(username + ':' + password),
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            // Credentials are valid
            currentUser = username;
            currentPassword = password;
            isAdmin = username === 'admin';

            // Show dashboard, hide login
            document.getElementById('loginSection').classList.add('login-hidden');
            document.getElementById('dashboardSection').classList.remove('dashboard-hidden');

            // Update user info in sidebar
            document.getElementById('currentUser').textContent = username === 'admin' ? 'Admin User' : 'Student User';

            // Clear login form
            document.getElementById('username').value = '';
            document.getElementById('password').value = '';

            // Show/hide admin buttons
            updateUIForRole();

            // Load initial data
            loadTeachers();
            showMessage(`Welcome ${username}!`, 'success');
        } else if (response.status === 401 || response.status === 403) {
            // Invalid credentials
            showLoginError('Invalid username or password. Please try again.');
        } else {
            // Other errors
            showLoginError('Login failed. Please try again.');
        }
    })
    .catch(error => {
        showLoginError('Connection error. Make sure the server is running.');
        console.error('Login error:', error);
    });
}

function showLoginError(message) {
    const errorDiv = document.getElementById('loginError');
    if (message) {
        errorDiv.textContent = message;
        errorDiv.classList.remove('hidden');
    } else {
        errorDiv.textContent = '';
        errorDiv.classList.add('hidden');
    }
}

function logout() {
    currentUser = null;
    currentPassword = null;
    isAdmin = false;

    // Show login, hide dashboard
    document.getElementById('loginSection').classList.remove('login-hidden');
    document.getElementById('dashboardSection').classList.add('dashboard-hidden');

    showMessage('Logged out successfully', 'success');
}

function updateUIForRole() {
    const addButtons = ['addTeacherBtn', 'addStudentBtn', 'addCourseBtn'];
    addButtons.forEach(btnId => {
        const btn = document.getElementById(btnId);
        if (btn) {
            btn.style.display = isAdmin ? 'flex' : 'none';
        }
    });
}

// API Helper
function fetchAPI(endpoint, options = {}) {
    const headers = {
        'Authorization': 'Basic ' + btoa(currentUser + ':' + currentPassword),
        'Content-Type': 'application/json',
        ...options.headers
    };

    return fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers
    });
}

// Message Display
function showMessage(text, type = 'success') {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;

    // Set colors based on type
    if (type === 'success') {
        messageDiv.className = 'fixed bottom-8 right-8 px-8 py-4 rounded-2xl shadow-2xl bg-gradient-to-r from-emerald-500 to-emerald-600 text-white transform translate-y-0 opacity-100 transition-all duration-300 z-50 font-bold border-2 border-emerald-400';
    } else {
        messageDiv.className = 'fixed bottom-8 right-8 px-8 py-4 rounded-2xl shadow-2xl bg-gradient-to-r from-rose-500 to-rose-600 text-white transform translate-y-0 opacity-100 transition-all duration-300 z-50 font-bold border-2 border-rose-400';
    }

    setTimeout(() => {
        messageDiv.className = 'fixed bottom-8 right-8 px-8 py-4 rounded-2xl shadow-2xl transform translate-y-20 opacity-0 transition-all duration-300 z-50 font-bold';
    }, 3000);
}

// Tab Management
function showTab(tabName) {
    // Remove active class from all tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => content.classList.add('hidden'));

    // Add active class to clicked tab button
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Show selected tab content
    document.getElementById(`${tabName}Tab`).classList.remove('hidden');

    // Load data for the selected tab
    if (tabName === 'teachers') {
        loadTeachers();
    } else if (tabName === 'students') {
        loadStudents();
    } else if (tabName === 'courses') {
        loadCourses();
    }
}

// Teachers
function loadTeachers() {
    const list = document.getElementById('teachersList');
    list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">Loading teachers...</div>';

    fetchAPI('/teachers')
        .then(response => response.json())
        .then(teachers => {
            if (teachers.length === 0) {
                list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">No teachers found. Click "Add Teacher" to create one!</div>';
                return;
            }

            list.innerHTML = teachers.map(teacher => `
                <div class="p-6 flex items-center justify-between hover:bg-gradient-to-r hover:from-primary/5 hover:to-transparent transition-all group">
                    <div class="flex items-center space-x-5">
                        <div class="w-16 h-16 bg-gradient-to-br from-primary to-secondary rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                            <span class="material-symbols-outlined text-white text-3xl">school</span>
                        </div>
                        <div>
                            <h3 class="font-bold text-lg mb-1">${teacher.name}</h3>
                            <p class="text-sm text-slate-500 dark:text-slate-400 mb-2">${teacher.email}</p>
                            <div class="flex items-center space-x-3 mt-2">
                                <span class="text-xs px-3 py-1.5 bg-gradient-to-r from-primary/10 to-primary/20 text-primary font-bold rounded-full border border-primary/30">${teacher.department}</span>
                                ${teacher.students ? `<span class="text-xs text-slate-400 font-semibold flex items-center"><span class="material-symbols-outlined text-sm mr-1">groups</span>${teacher.students.length} Students</span>` : ''}
                                ${teacher.courses ? `<span class="text-xs text-slate-400 font-semibold flex items-center"><span class="material-symbols-outlined text-sm mr-1">menu_book</span>${teacher.courses.length} Courses</span>` : ''}
                            </div>
                        </div>
                    </div>
                    ${isAdmin ? `
                    <button onclick="deleteTeacher(${teacher.id})" class="p-3 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all hover:scale-110">
                        <span class="material-symbols-outlined text-2xl">delete</span>
                    </button>
                    ` : ''}
                </div>
            `).join('');
        })
        .catch(error => {
            list.innerHTML = '<div class="p-12 text-center text-red-500 font-medium">Error loading teachers</div>';
            console.error('Error:', error);
        });
}

function showAddTeacherForm() {
    document.getElementById('addTeacherModal').classList.remove('hidden');
}

function hideAddTeacherForm() {
    document.getElementById('addTeacherModal').classList.add('hidden');
    document.getElementById('teacherName').value = '';
    document.getElementById('teacherEmail').value = '';
    document.getElementById('teacherDepartment').value = '';
}

function addTeacher(event) {
    event.preventDefault();

    const teacher = {
        name: document.getElementById('teacherName').value,
        email: document.getElementById('teacherEmail').value,
        department: document.getElementById('teacherDepartment').value
    };

    fetchAPI('/teachers', {
        method: 'POST',
        body: JSON.stringify(teacher)
    })
    .then(response => {
        if (response.ok) {
            showMessage('Teacher added successfully', 'success');
            hideAddTeacherForm();
            loadTeachers();
        } else {
            showMessage('Failed to add teacher', 'error');
        }
    })
    .catch(error => {
        showMessage('Error adding teacher', 'error');
        console.error('Error:', error);
    });
}

function deleteTeacher(id) {
    if (!confirm('Are you sure you want to delete this teacher? This will also delete all associated students and courses.')) {
        return;
    }

    fetchAPI(`/teachers/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                showMessage('Teacher deleted successfully', 'success');
                loadTeachers();
            } else {
                showMessage('Failed to delete teacher', 'error');
            }
        })
        .catch(error => {
            showMessage('Error deleting teacher', 'error');
            console.error('Error:', error);
        });
}

// Students
function loadStudents() {
    const list = document.getElementById('studentsList');
    list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">Loading students...</div>';

    fetchAPI('/students')
        .then(response => response.json())
        .then(students => {
            if (students.length === 0) {
                list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">No students found. Click "Add Student" to enroll one!</div>';
                return;
            }

            list.innerHTML = students.map(student => `
                <div class="p-6 flex items-center justify-between hover:bg-gradient-to-r hover:from-emerald-500/5 hover:to-transparent transition-all group">
                    <div class="flex items-center space-x-5">
                        <div class="w-16 h-16 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                            <span class="material-symbols-outlined text-white text-3xl">person</span>
                        </div>
                        <div>
                            <h3 class="font-bold text-lg mb-1">${student.name}</h3>
                            <p class="text-sm text-slate-500 dark:text-slate-400 mb-2">${student.email}</p>
                            <span class="text-xs px-3 py-1.5 bg-gradient-to-r from-emerald-500/10 to-emerald-500/20 text-emerald-700 dark:text-emerald-400 font-bold rounded-full border border-emerald-500/30">ID: ${student.studentId}</span>
                        </div>
                    </div>
                    ${isAdmin ? `
                    <button onclick="deleteStudent(${student.id})" class="p-3 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all hover:scale-110">
                        <span class="material-symbols-outlined text-2xl">delete</span>
                    </button>
                    ` : ''}
                </div>
            `).join('');
        })
        .catch(error => {
            list.innerHTML = '<div class="p-12 text-center text-red-500 font-medium">Error loading students</div>';
            console.error('Error:', error);
        });
}

function showAddStudentForm() {
    document.getElementById('addStudentModal').classList.remove('hidden');
    loadTeachersForSelect('studentTeacher');
}

function hideAddStudentForm() {
    document.getElementById('addStudentModal').classList.add('hidden');
    document.getElementById('studentName').value = '';
    document.getElementById('studentEmail').value = '';
    document.getElementById('studentId').value = '';
    document.getElementById('studentTeacher').value = '';
}

function addStudent(event) {
    event.preventDefault();

    const teacherId = document.getElementById('studentTeacher').value;
    const student = {
        name: document.getElementById('studentName').value,
        email: document.getElementById('studentEmail').value,
        studentId: document.getElementById('studentId').value
    };

    fetchAPI(`/students/teacher/${teacherId}`, {
        method: 'POST',
        body: JSON.stringify(student)
    })
    .then(response => {
        if (response.ok) {
            showMessage('Student added successfully', 'success');
            hideAddStudentForm();
            loadStudents();
        } else {
            showMessage('Failed to add student', 'error');
        }
    })
    .catch(error => {
        showMessage('Error adding student', 'error');
        console.error('Error:', error);
    });
}

function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) {
        return;
    }

    fetchAPI(`/students/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                showMessage('Student deleted successfully', 'success');
                loadStudents();
            } else {
                showMessage('Failed to delete student', 'error');
            }
        })
        .catch(error => {
            showMessage('Error deleting student', 'error');
            console.error('Error:', error);
        });
}

// Courses
function loadCourses() {
    const list = document.getElementById('coursesList');
    list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">Loading courses...</div>';

    fetchAPI('/courses')
        .then(response => response.json())
        .then(courses => {
            if (courses.length === 0) {
                list.innerHTML = '<div class="p-12 text-center text-slate-500 font-medium">No courses found. Click "Add Course" to create one!</div>';
                return;
            }

            list.innerHTML = courses.map(course => `
                <div class="p-6 flex items-center justify-between hover:bg-gradient-to-r hover:from-amber-500/5 hover:to-transparent transition-all group">
                    <div class="flex items-center space-x-5">
                        <div class="w-16 h-16 bg-gradient-to-br from-amber-500 to-orange-600 rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                            <span class="material-symbols-outlined text-white text-3xl">menu_book</span>
                        </div>
                        <div>
                            <h3 class="font-bold text-lg mb-1">${course.title}</h3>
                            <p class="text-sm text-slate-500 dark:text-slate-400 mb-2">Code: ${course.courseCode}</p>
                            <span class="text-xs px-3 py-1.5 bg-gradient-to-r from-amber-500/10 to-amber-500/20 text-amber-700 dark:text-amber-400 font-bold rounded-full border border-amber-500/30">${course.credits} Credits</span>
                        </div>
                    </div>
                    ${isAdmin ? `
                    <button onclick="deleteCourse(${course.id})" class="p-3 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all hover:scale-110">
                        <span class="material-symbols-outlined text-2xl">delete</span>
                    </button>
                    ` : ''}
                </div>
            `).join('');
        })
        .catch(error => {
            list.innerHTML = '<div class="p-12 text-center text-red-500 font-medium">Error loading courses</div>';
            console.error('Error:', error);
        });
}

function showAddCourseForm() {
    document.getElementById('addCourseModal').classList.remove('hidden');
    loadTeachersForSelect('courseTeacher');
}

function hideAddCourseForm() {
    document.getElementById('addCourseModal').classList.add('hidden');
    document.getElementById('courseTitle').value = '';
    document.getElementById('courseCode').value = '';
    document.getElementById('courseCredits').value = '';
    document.getElementById('courseTeacher').value = '';
}

function addCourse(event) {
    event.preventDefault();

    const teacherId = document.getElementById('courseTeacher').value;
    const course = {
        title: document.getElementById('courseTitle').value,
        courseCode: document.getElementById('courseCode').value,
        credits: parseInt(document.getElementById('courseCredits').value)
    };

    fetchAPI(`/courses/teacher/${teacherId}`, {
        method: 'POST',
        body: JSON.stringify(course)
    })
    .then(response => {
        if (response.ok) {
            showMessage('Course added successfully', 'success');
            hideAddCourseForm();
            loadCourses();
        } else {
            showMessage('Failed to add course', 'error');
        }
    })
    .catch(error => {
        showMessage('Error adding course', 'error');
        console.error('Error:', error);
    });
}

function deleteCourse(id) {
    if (!confirm('Are you sure you want to delete this course?')) {
        return;
    }

    fetchAPI(`/courses/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                showMessage('Course deleted successfully', 'success');
                loadCourses();
            } else {
                showMessage('Failed to delete course', 'error');
            }
        })
        .catch(error => {
            showMessage('Error deleting course', 'error');
            console.error('Error:', error);
        });
}

// Helper function to load teachers for select dropdowns
function loadTeachersForSelect(selectId) {
    const select = document.getElementById(selectId);

    fetchAPI('/teachers')
        .then(response => response.json())
        .then(teachers => {
            select.innerHTML = '<option value="">Select Teacher</option>' +
                teachers.map(teacher => `<option value="${teacher.id}">${teacher.name}</option>`).join('');
        })
        .catch(error => {
            console.error('Error loading teachers for select:', error);
        });
}
