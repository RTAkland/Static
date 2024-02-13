#!/usr/bin/bash

echo -n "输入新用户名称: "
read user
command="saslpasswd2 -c -u byteas.top $user"
$command

cp -a /etc/sasldb2 /var/spool/postfix/etc/
gpasswd -a postfix sasl

echo "新用户添加完成"
