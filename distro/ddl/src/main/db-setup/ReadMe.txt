This directory contains setup instructions and scripts for required live databases

####### DB Setup

## MYSQL

$ vi /etc/my.cnf
	bind-address=0.0.0.0

# You'll need the MySQL root user password (by default not set on new server)
$ mysql -u root -p
mysql> GRANT ALL PRIVILEGES ON *.* TO 'apiman'@'%' IDENTIFIED BY 'apiman';
mysql> FLUSH PRIVILEGES;
mysql> CREATE DATABASE IF NOT EXISTS apiman_empty;
mysql> CREATE DATABASE IF NOT EXISTS apiman_current;
mysql> CREATE DATABASE IF NOT EXISTS apiman_previous;
mysql> quit;


## PostgreSQL

$ vi /var/lib/pgsql/9.3/data/postgresql.conf

	listen_addresses = '*'

$ vi /var/lib/pgsql/9.3/data/pg_hba.conf

	# IPv4 local connections:
    host    all             apiman          0.0.0.0/0               md5           <---- add this BEFORE previous line in original file
    host    all             all             127.0.0.1/32            ident

$ su - postgres
$ createuser -U postgres -d -e -E -l -P -r apiman
    Enter password for new role: apiman
    Enter it again: apiman
$ createdb -O apiman apiman_empty
$ createdb -O apiman apiman_current
$ createdb -O apiman apiman_previous