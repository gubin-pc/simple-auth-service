
function signUp() {
    let xhr = new XMLHttpRequest();
    let data = JSON.stringify(Object.fromEntries(new FormData(signup)));

    xhr.open('POST', '/sign_up');
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(data);
    xhr.onload = function() {
        if (xhr.status >= 400) {
            alert(`Ошибка ${xhr.status}: ${xhr.statusText}`);
        } else {
            document.getElementById("chk").checked = true;
            document.getElementById('username_login').value = JSON.parse(xhr.response).username;
        }
    };
}

function signIn() {
    let xhr = new XMLHttpRequest();
    let data = Object.fromEntries(new FormData(signin))

    xhr.open('POST', '/sign_in', false, data.username, data.password);
    xhr.send()
    if (xhr.status >= 400) {
        alert(`Ошибка ${xhr.status}: ${xhr.statusText}`);
    } else {
        window.location.href = '/account'
    }
}

function logout() {
    let xhr = new XMLHttpRequest();
    xhr.open('POST', '/logout');
    xhr.send();
    xhr.onload = function() {
        if (xhr.status >= 400) {
            alert(`Ошибка ${xhr.status}: ${xhr.statusText}`);
        } else {
            window.location.href = "/"
        }
    };
}

function accountRoleEndpoint(role) {
    let xhr = new XMLHttpRequest();
    let url = "/" + role.toLowerCase();
    xhr.open('GET', url);
    xhr.send();
    xhr.onload = function() {
        if (xhr.status >= 400) {
            alert(`Ошибка ${xhr.status}: ${xhr.statusText}\n`);
        } else {
            alert(xhr.response)
        }
    };
}

function changePassword() {
    let xhr = new XMLHttpRequest();
    let data = JSON.stringify(Object.fromEntries(new FormData(reset)));

    xhr.open('POST', '/change_password');
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(data);
    xhr.onload = function() {
        if (xhr.status >= 400) {
            alert(`Ошибка ${xhr.status}: ${xhr.statusText}`);
        } else {
            window.location.href = "/"
            document.getElementById("chk").checked = true;
        }
    };
}