dnl $Id$
dnl config.m4 for extension zmk_file

dnl Comments in this file start with the string 'dnl'.
dnl Remove where necessary. This file will not work
dnl without editing.

dnl If your extension references something external, use with:

PHP_ARG_WITH(zmk_file, for zmk_file support,
dnl Make sure that the comment is aligned:
[  --with-zmk_file             Include zmk_file support])

dnl Otherwise use enable:

dnl PHP_ARG_ENABLE(zmk_file, whether to enable zmk_file support,
dnl Make sure that the comment is aligned:
dnl [  --enable-zmk_file           Enable zmk_file support])

if test "$PHP_ZMK_FILE" != "no"; then
  dnl Write more examples of tests here...

  dnl # --with-zmk_file -> check with-path
  dnl SEARCH_PATH="/usr/local /usr"     # you might want to change this
  dnl SEARCH_FOR="/include/zmk_file.h"  # you most likely want to change this
  dnl if test -r $PHP_ZMK_FILE/$SEARCH_FOR; then # path given as parameter
  dnl   ZMK_FILE_DIR=$PHP_ZMK_FILE
  dnl else # search default path list
  dnl   AC_MSG_CHECKING([for zmk_file files in default path])
  dnl   for i in $SEARCH_PATH ; do
  dnl     if test -r $i/$SEARCH_FOR; then
  dnl       ZMK_FILE_DIR=$i
  dnl       AC_MSG_RESULT(found in $i)
  dnl     fi
  dnl   done
  dnl fi
  dnl
  dnl if test -z "$ZMK_FILE_DIR"; then
  dnl   AC_MSG_RESULT([not found])
  dnl   AC_MSG_ERROR([Please reinstall the zmk_file distribution])
  dnl fi

  dnl # --with-zmk_file -> add include path
  dnl PHP_ADD_INCLUDE($ZMK_FILE_DIR/include)

  dnl # --with-zmk_file -> check for lib and symbol presence
  dnl LIBNAME=zmk_file # you may want to change this
  dnl LIBSYMBOL=zmk_file # you most likely want to change this 

  dnl PHP_CHECK_LIBRARY($LIBNAME,$LIBSYMBOL,
  dnl [
  dnl   PHP_ADD_LIBRARY_WITH_PATH($LIBNAME, $ZMK_FILE_DIR/$PHP_LIBDIR, ZMK_FILE_SHARED_LIBADD)
  dnl   AC_DEFINE(HAVE_ZMK_FILELIB,1,[ ])
  dnl ],[
  dnl   AC_MSG_ERROR([wrong zmk_file lib version or lib not found])
  dnl ],[
  dnl   -L$ZMK_FILE_DIR/$PHP_LIBDIR -lm
  dnl ])
  dnl
  dnl PHP_SUBST(ZMK_FILE_SHARED_LIBADD)

  PHP_NEW_EXTENSION(zmk_file, zmk_file.c, $ext_shared)
fi
