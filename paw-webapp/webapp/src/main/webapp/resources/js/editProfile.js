window.onload = function() {
    document.getElementById('editUserForm').onsubmit = function() {
        let list = document.getElementById(this.getAttribute('data-list')); // only needed if more than one form or list
        let listItem = document.createElement("li");
        let inputItem = this.experience;
        listItem.innerText = inputItem.value;
        list.appendChild(listItem);
        inputItem.select();
        inputItem.focus();
        return false; // stop submission
    }
    document.getElementById("experience").focus();
}