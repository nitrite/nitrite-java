#!/bin/sh
#
# ---------------------------------------------------------------------
# Nitrite Data Gate startup script.
# ---------------------------------------------------------------------

message()
{
  TITLE="Cannot start Nitrite Data Gate server"
  if [ -n "`which zenity`" ]; then
    zenity --error --title="$TITLE" --text="$1"
  elif [ -n "`which kdialog`" ]; then
    kdialog --error "$1" --title "$TITLE"
  elif [ -n "`which xmessage`" ]; then
    xmessage -center "ERROR: $TITLE: $1"
  elif [ -n "`which notify-send`" ]; then
    notify-send "ERROR: $TITLE: $1"
  else
    echo "ERROR: $TITLE\n$1"
  fi
}

UNAME=`which uname`
GREP=`which egrep`
CUT=`which cut`
READLINK=`which readlink`
XARGS=`which xargs`
DIRNAME=`which dirname`

OS_TYPE=`"$UNAME" -s`

#
# ---------------------------------------------------------------------
# Ensure DATAGATE_HOME points to the directory where the Data Gate is installed.
# ---------------------------------------------------------------------

SCRIPT_LOCATION=$0
if [ -x "$READLINK" ]; then
  while [ -L "$SCRIPT_LOCATION" ]; do
    SCRIPT_LOCATION=`"$READLINK" -e "$SCRIPT_LOCATION"`
  done
fi

DATAGATE_BIN_DIR=`dirname "$SCRIPT_LOCATION"`
if [ "$DATAGATE_BIN_DIR" = "." ]; then
  DATAGATE_HOME=".."
else
  DATAGATE_HOME=`dirname "$DATAGATE_BIN_DIR"`
fi

DATAGATE_LIB_DIR="$DATAGATE_HOME"/lib
DATAGATE_EXE="$DATAGATE_LIB_DIR"/nitrite-datagate.jar
DATAGATE_CONF="$DATAGATE_HOME"/conf/datagate.properties
DATAGATE_LOG_DIR="$DATAGATE_HOME"/logs

if [ -n "$JDK_HOME" -a -x "$JDK_HOME/bin/java" ]; then
  JDK="$JDK_HOME"
elif [ -n "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ]; then
  JDK="$JAVA_HOME"
else
  JAVA_BIN_PATH=`which java`
  if [ -n "$JAVA_BIN_PATH" ]; then
    if [ "$OS_TYPE" = "FreeBSD" -o "$OS_TYPE" = "MidnightBSD" ]; then
      JAVA_LOCATION=`JAVAVM_DRYRUN=yes java | "$GREP" '^JAVA_HOME' | "$CUT" -c11-`
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    elif [ "$OS_TYPE" = "SunOS" ]; then
      JAVA_LOCATION="/usr/jdk/latest"
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    elif [ "$OS_TYPE" = "Darwin" ]; then
      JAVA_LOCATION=`/usr/libexec/java_home`
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    fi

    if [ -z "$JDK" -a -x "$READLINK" -a -x "$XARGS" -a -x "$DIRNAME" ]; then
      JAVA_LOCATION=`"$READLINK" -f "$JAVA_BIN_PATH"`
      case "$JAVA_LOCATION" in
        */jre/bin/java)
          JAVA_LOCATION=`echo "$JAVA_LOCATION" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME"`
          if [ ! -d "$JAVA_LOCATION/bin" ]; then
            JAVA_LOCATION="$JAVA_LOCATION/jre"
          fi
          ;;
        *)
          JAVA_LOCATION=`echo "$JAVA_LOCATION" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME"`
          ;;
      esac
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    fi
  fi
fi

JAVA_BIN="$JDK/bin/java"
if [ ! -x "$JAVA_BIN" ]; then
  JAVA_BIN="$JDK/jre/bin/java"
fi

if [ -z "$JDK" ] || [ ! -x "$JAVA_BIN" ]; then
  message "No JDK found. Please validate either JDK_HOME or JAVA_HOME environment variable points to valid JDK installation."
  exit 1
fi

mkdir -p "$DATAGATE_LOG_DIR"

JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# ---------------------------------------------------------------------
# Run the Server.
# ---------------------------------------------------------------------

"$JAVA_BIN" "$JAVA_OPTS" -jar "$DATAGATE_EXE" --spring.config.location="$DATAGATE_CONF" --datagate.log.dir="$DATAGATE_LOG_DIR"