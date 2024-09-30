import inspect
import stat
from argparse import ArgumentParser
from pathlib import Path

windows_template = inspect.cleandoc("""
    @echo off
    set VM_OPTIONS=
    start "" "{java_path}javaw" %VM_OPTIONS% {launch_cmd} %*""")

linux_template = inspect.cleandoc("""
    #!/bin/sh
    VM_OPTIONS=
    BASEDIR=$(dirname "$0")
    "{java_path}java" $VM_OPTIONS {launch_cmd} $*""")    

if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("platform", choices=["windows", "linux"])
    parser.add_argument("app", choices=["src", "info"])
    parser.add_argument("runtime_type", choices=["local", "img"])
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    java_path: str
    match args.runtime_type, args.platform:
        case ("local", _): java_path = ""
        case ("img", "windows"): java_path = "%~dp0\\bin\\"
        case ("img", "linux"): java_path = "$BASEDIR/bin/"
    
    main_class: str
    match args.app:
        case "src": main_class = "info.ata4.bspsrc.app.src.BspSourceLauncher"
        case "info": main_class = "info.ata4.bspsrc.app.info.BspInfo"

    launch_cmd: str
    match args.runtime_type, args.platform:
        case ("local", "windows"):launch_cmd = f"-cp \"%~dp0\\bspsrc.jar\" {main_class}"
        case ("local", "linux"): launch_cmd = f"-cp \"$DIR/bspsrc.jar\" {main_class}"
        case ("img", _): launch_cmd = f"-m info.ata4.bspsrc.app/{main_class}"
    
    script: str
    match args.platform:
        case "windows": script = windows_template.format(java_path=java_path, launch_cmd=launch_cmd)
        case "linux": script = linux_template.format(java_path=java_path, launch_cmd=launch_cmd)
    
    linefeed: str
    match args.platform:
        case "windows": linefeed = "\r\n"
        case "linux": linefeed = "\n"
    
    out: Path = args.output
    with out.open(mode="x", newline=linefeed) as file:
        file.write(script)
    if args.platform == "linux":
        out.chmod(out.stat().st_mode | stat.S_IEXEC)
    