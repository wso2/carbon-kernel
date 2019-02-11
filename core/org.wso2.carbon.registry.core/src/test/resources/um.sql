####################### inserts
add.user=insert into um_users (user_name, password, id) values (?, ?, ?)
add.role=insert into um_roles (role_name, id) values (?, ?)
add.user.role=insert into um_user_roles (user_id, role_id) values (?, ?)
add.role.attribute=insert into um_role_attributes (attr_name, attr_value, role_id, id) values (?, ?, ?, ?)
add.user.attribute=insert into um_user_attributes (attr_name, attr_value, user_id, id) values (?, ?, ?, ?)
add.permission=insert into um_permissions (resource_id, action, id) values (?, ?, ?)
add.role.permission=insert into um_role_permissions (permission_id, is_allowed, role_id, id) values (?, ?, ?, ?)
add.user.permission=insert into um_user_permissions (permission_id, is_allowed, user_id, id) values (?, ?, ?, ?)
####################### update
update.user=update um_users set password= ? where user_name= ?
####################### delete
delete.user=delete from um_users where user_name = ?
delete.role=delete from um_roles where role_name = ?
delete.user.role=delete from um_user_roles where user_id=(select id from um_users where user_name=?) and role_id=(select id from um_roles where role_name=?)
delete.role.attribute=delete from um_role_attributes where role_id = ?
delete.user.attribute=delete from um_user_attributes where user_id = ?
delete.permission.resource=delete from um_permissions where resource_id = ?
delete.user.permission=delete from um_user_permissions where user_id IN (select um_user_permissions.user_id from um_user_permissions, um_users, um_permissions where um_user_permissions.user_id=um_users.id and um_user_permissions.permission_id=um_permissions.id and um_permissions.resource_id=? and um_permissions.action=? and um_users.user_name=?)
delete.role.permission=delete from um_role_permissions where role_id IN (select um_role_permissions.role_id from um_role_permissions, um_roles, um_permissions where um_role_permissions.role_id=um_roles.id and um_role_permissions.permission_id=um_permissions.id and um_permissions.resource_id=? and um_permissions.action=? and um_roles.role_name=?)
####################### querries
get.user=select * from um_users where user_name=?
get.role=select * from um_roles where role_name=?
get.user.id=select id from um_users where user_name=?
get.role.id=select id from um_roles where role_name=?
get.user.roles= select um_roles.role_name from um_user_roles, um_roles, um_users where um_users.user_name=? and um_users.id=um_user_roles.user_id and um_roles.id=um_user_roles.role_id
get.role.attributes= select * from um_role_attributes, um_roles where um_roles.id = um_role_attributes.role_id and um_roles.role_name=?
get.user.attributes= select * from um_user_attributes, um_users where um_users.id = um_user_attributes.user_id and um_users.user_name=?
get.permission=select id from um_permissions where um_permissions.resource_id=? and um_permissions.action=?
get.role.allowed=select um_role_permissions.is_allowed from um_role_permissions, um_permissions, um_roles where um_role_permissions.role_id=um_roles.id and um_role_permissions.permission_id=um_permissions.id and um_permissions.resource_id=? and um_permissions.action=? and um_roles.role_name=?
get.user.allowed=select um_user_permissions.is_allowed from um_user_permissions, um_permissions, um_users where um_user_permissions.user_id=um_users.id and um_user_permissions.permission_id=um_permissions.id and um_permissions.resource_id=? and um_permissions.action=? and um_users.user_name=?
get.resource.role=select um_roles.role_name from um_role_permissions, um_permissions, um_roles where um_permissions.resource_id=? and um_permissions.action=? and um_permissions.id=um_role_permissions.permission_id and um_role_permissions.role_id=um_roles.id
get.resource.user=select um_users.user_name from um_user_permissions, um_permissions, um_users where um_permissions.resource_id=? and um_permissions.action=? and um_permissions.id=um_user_permissions.permission_id and um_user_permissions.user_id=um_users.id
get.role.permission=select * from um_role_permissions where permission_id=?
get.user.permission=select * from um_user_permissions where permission_id=?
get.roles.all=select role_name from um_roles
get.users.all=select user_name from um_users
get.distinct.attribute.names = select distinct attr_name from  um_user_attributes
####################### max
max.user=select max(id) from um_users
max.role=select max(id) from um_roles
max.user.attribute=select max(id) from um_role_attributes
max.role.attribute=select max(id) from um_user_attributes
max.permission=select max(id) from um_permissions
max.role.permission=select max(id) from um_role_permissions
max.user.permission=select max(id) from um_user_permissions
###################### copy resource permissions
get.resource.permission = select * from um_permissions where um_permissions.resource_id=?
