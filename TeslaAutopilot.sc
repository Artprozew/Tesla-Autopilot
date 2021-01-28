SCRIPT_START
{
NOP

LVAR_INT scplayer, veh, autopilot, autopiloton, autopilotoff, warning, char, vid, ivar, veh2
LVAR_INT char2, autodrive, model[4], mode
LVAR_FLOAT sfxvolume, x, y, z, x2, y2, z2, vspeed, sphere, offset, multirange, vspeed2
LVAR_FLOAT fvar, autospeed, brakespeed

LOAD_AUDIO_STREAM "CLEO\TeslaAutopilot\autopiloton.mp3", autopiloton
LOAD_AUDIO_STREAM "CLEO\TeslaAutopilot\autopilotoff.mp3", autopilotoff
LOAD_AUDIO_STREAM "CLEO\TeslaAutopilot\warning.mp3", warning

IF NOT DOES_FILE_EXIST "CLEO\TeslaAutopilot.ini"
    model[0] = -1
    WRITE_INT_TO_INI_FILE model[0], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model1"
    model[1] = model[0]
    WRITE_INT_TO_INI_FILE model[1], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model2"
    model[2] = model[1]
    WRITE_INT_TO_INI_FILE model[2], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model3"
    model[3] = model[2]
    WRITE_INT_TO_INI_FILE model[3], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model4"
    sphere = 2.7
    WRITE_FLOAT_TO_INI_FILE sphere, "CLEO\TeslaAutopilot.ini", "Config", "SphereSize"
    multirange = 1.5
    WRITE_FLOAT_TO_INI_FILE multirange, "CLEO\TeslaAutopilot.ini", "Config", "MultiplicateRange"
    brakespeed = 0.5
    WRITE_FLOAT_TO_INI_FILE brakespeed, "CLEO\TeslaAutopilot.ini", "Config", "BrakeStrength"
ENDIF
IF NOT READ_INT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Model IDs", "Model1", model[0]
    WHILE NOT IS_PLAYER_PLAYING 0
        WAIT 0
    ENDWHILE
    WAIT 2000
    PRINT_STRING_NOW "Nao foi possivel ler o ID do 'Model1' no 'CLEO\TeslaAutopilot.ini'", 8000
    TERMINATE_THIS_CUSTOM_SCRIPT
ENDIF
IF NOT READ_INT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Model IDs", "Model2", model[1]
    model[1] = model[0]
    WRITE_INT_TO_INI_FILE model[1], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model2"
ENDIF
IF NOT READ_INT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Model IDs", "Model3", model[2]
    model[2] = model[1]
    WRITE_INT_TO_INI_FILE model[2], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model3"
ENDIF
IF NOT READ_INT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Model IDs", "Model4", model[3]
    model[3] = model[2]
    WRITE_INT_TO_INI_FILE model[3], "CLEO\TeslaAutopilot.ini", "Model IDs", "Model4"
ENDIF
// ----------------------------------------------------------------------------
IF NOT READ_FLOAT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Config", "SphereSize", sphere
    sphere = 2.7
    WRITE_FLOAT_TO_INI_FILE sphere, "CLEO\TeslaAutopilot.ini", "Config", "SphereSize"
ENDIF
IF NOT READ_FLOAT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Config", "MultiplicateRange", multirange
    multirange = 1.5
    WRITE_FLOAT_TO_INI_FILE multirange, "CLEO\TeslaAutopilot.ini", "Config", "MultiplicateRange"
ENDIF
IF NOT READ_FLOAT_FROM_INI_FILE "CLEO\TeslaAutopilot.ini", "Config", "BrakeStrength", brakespeed
    brakespeed = 0.5
    WRITE_FLOAT_TO_INI_FILE brakespeed, "CLEO\TeslaAutopilot.ini", "Config", "BrakeStrength"
ENDIF

GET_PLAYER_CHAR 0, scplayer
autospeed = 20.0

main_loop:
WAIT 0

IF NOT IS_CHAR_IN_ANY_CAR scplayer
AND veh = 0
    autopilot = FALSE
    WAIT 5000
    GOTO main_loop
ENDIF

IF IS_CHAR_IN_ANY_CAR scplayer
    STORE_CAR_CHAR_IS_IN_NO_SAVE scplayer, veh
    GET_CAR_MODEL veh, vid
    IF NOT vid = model[0]
    AND NOT vid = model[1]
    AND NOT vid = model[2]
    AND NOT vid = model[3]
        veh = 0
        autopilot = FALSE
        autodrive = FALSE
        WAIT 5000
        GOTO main_loop
    ENDIF
ELSE
    IF NOT vid = model[0]
    AND NOT vid = model[1]
    AND NOT vid = model[2]
    AND NOT vid = model[3]
        veh = 0
        autopilot = FALSE
        autodrive = FALSE
        WAIT 5000
        GOTO main_loop
    ENDIF
    IF NOT autodrive = TRUE
        autopilot = FALSE
    ENDIF
    IF GOSUB checkvehicle
        veh = 0
        GOTO main_loop
    ENDIF
    IF NOT veh = 0
        IF IS_KEY_PRESSED VK_TAB
            GET_DRIVER_OF_CAR veh, char2
            IF DOES_CHAR_EXIST char2
                SET_CHAR_MODEL_ALPHA char2, 255 // Resolve um bug que os peds ficavam aparecendo invisiveis?
                DELETE_CHAR char2
            ENDIF
            //CREATE_RANDOM_CHAR_AS_DRIVER veh, char2  // Essa função pegava um char aleatório na rua e o teleportava pro carro, então um char sumia do nada da tela??
            CREATE_CHAR_INSIDE_CAR veh, PEDTYPE_CIVMALE, MALE01, char2
            SET_CAR_ENGINE_ON veh, 1
            CLOSE_ALL_CAR_DOORS veh
            SET_CHAR_MODEL_ALPHA char2, 0
            SET_CAR_DRIVING_STYLE veh, 2
            GET_CHAR_COORDINATES scplayer, x, y, z
            GET_GROUND_Z_FOR_3D_COORD x, y, z, z
            TASK_CAR_DRIVE_TO_COORD char2, veh, x, y, z, 20.0, 1, 1, 1
            SET_CAR_CAN_GO_AGAINST_TRAFFIC veh, 1
            autopilot = TRUE
            autodrive = TRUE
            WHILE IS_KEY_PRESSED VK_KEY_K
                WAIT 0
            ENDWHILE
        ENDIF
        WHILE IS_KEY_PRESSED VK_TAB
            WAIT 0
        ENDWHILE
    ENDIF
ENDIF

IF autodrive = TRUE
    IF DOES_CHAR_EXIST char2
        IF NOT IS_CHAR_IN_ANY_CAR char2
            SET_CHAR_MODEL_ALPHA char2, 255
            DELETE_CHAR char2
            autopilot = FALSE
            autodrive = FALSE
            GOTO main_loop
        ELSE
            GET_CHAR_COORDINATES char2, x, y, z
            GET_CHAR_COORDINATES scplayer, x2, y2, z2
            GET_DISTANCE_BETWEEN_COORDS_2D x, y, x2, y2, fvar
            IF fvar > 300.0
                IF DOES_CHAR_EXIST char2
                    SET_CHAR_MODEL_ALPHA char2, 255
                    DELETE_CHAR char2
                    autopilot = FALSE
                    autodrive = FALSE
                    GOTO main_loop
                ENDIF
            ELSE
                IF IS_CAR_STUCK veh
                    SET_CAR_FORWARD_SPEED veh, -5.0
                ENDIF
            ENDIF
        ENDIF
    ENDIF
ENDIF

GET_AUDIO_SFX_VOLUME sfxvolume
GET_CAR_SPEED veh, vspeed
GET_CAR_PEDALS veh, x, y

IF FRAME_MOD 2
AND NOT x < 0.0
AND NOT IS_CAR_REALLY_IN_AIR veh
AND IS_CHAR_IN_ANY_CAR scplayer
    char = -1
    offset = vspeed * multirange
    IF offset > 80.0
        offset = 80.0
    ENDIF
    GET_OFFSET_FROM_CAR_IN_WORLD_COORDS veh, 0.0, offset, 0.0, x, y, z
    IF GET_RANDOM_CHAR_IN_SPHERE_NO_SAVE_RECURSIVE x, y, z, sphere, 1, 1, char
        IF IS_CHAR_IN_ANY_CAR char
            STORE_CAR_CHAR_IS_IN_NO_SAVE char, veh2
            GET_CAR_SPEED veh2, vspeed2
            IF autopilot = TRUE
                IF autodrive = FALSE
                    IF mode = 0
                    OR mode = 4
                        IF vspeed > vspeed2
                            SET_CAR_CRUISE_SPEED veh, vspeed2
                        ENDIF
                    ENDIF
                ENDIF
            ELSE
                vspeed2 *= 1.15
                IF vspeed > vspeed2
                AND NOT vspeed < 8.0
                    GET_AUDIO_STREAM_STATE warning, ivar
                    IF NOT ivar = 1
                        SET_AUDIO_STREAM_STATE warning, 1
                        SET_AUDIO_STREAM_VOLUME warning, sfxvolume
                    ENDIF
                ENDIF
                vspeed2 /= 1.15
            ENDIF
        ELSE
            IF NOT vspeed < 8.0
                GET_AUDIO_STREAM_STATE warning, ivar
                IF NOT ivar = 1
                AND NOT autopilot = TRUE
                    SET_AUDIO_STREAM_STATE warning, 1
                    SET_AUDIO_STREAM_VOLUME warning, sfxvolume
                ENDIF
            ENDIF
        ENDIF
    ELSE
        GET_OFFSET_FROM_CAR_IN_WORLD_COORDS veh, 0.0, 10.0, 0.0, x, y, z
        IF GET_RANDOM_CHAR_IN_SPHERE_NO_SAVE_RECURSIVE x, y, z, sphere, 1, 1, char
            GET_AUDIO_STREAM_STATE warning, ivar
            IF NOT ivar = 1
            AND NOT vspeed < 8.0
                IF NOT autopilot = TRUE
                    SET_AUDIO_STREAM_STATE warning, 1
                    SET_AUDIO_STREAM_VOLUME warning, sfxvolume
                ENDIF
                SET_CAR_LIGHTS_ON veh, 1 // Por algum motivo não funciona aqui?
                WHILE TRUE
                    WAIT 0
                    IF NOT mode = 0
                    AND NOT mode = 4
                    AND autopilot = TRUE
                        BREAK
                    ENDIF
                    IF autodrive = TRUE
                        BREAK
                    ENDIF
                    IF NOT IS_CHAR_IN_CAR scplayer, veh
                    OR GOSUB checkvehicle
                        GOTO main_loop
                    ENDIF
                    GET_CAR_SPEED veh, vspeed
                    IF IS_CHAR_IN_ANY_CAR char
                        STORE_CAR_CHAR_IS_IN_NO_SAVE char, veh2
                        GET_CAR_SPEED veh2, vspeed2
                        IF vspeed <= vspeed2
                            IF autopilot = TRUE
                                SET_CAR_CRUISE_SPEED veh, vspeed2
                            ENDIF
                            BREAK
                        ENDIF
                    ENDIF
                    IF vspeed <= 1.0
                        SET_CAR_FORWARD_SPEED veh, 0.0
                        BREAK
                    ENDIF
                    vspeed -=@ brakespeed
                    SET_CAR_FORWARD_SPEED veh, vspeed
                ENDWHILE
                SET_CAR_LIGHTS_ON veh, 0
                GET_CAR_SPEED veh, vspeed
                WAIT 2000
                //APPLY_BRAKES_TO_PLAYERS_CAR 0, 1 // Por algum motivo isso também faz o carro da frente frear
            ENDIF
        ELSE
            IF autopilot = TRUE
                SET_CAR_CRUISE_SPEED veh, autospeed
            ENDIF
        ENDIF
    ENDIF
ENDIF

IF autopilot = TRUE
AND NOT autodrive = TRUE
    IF IS_KEY_PRESSED VK_KEY_W
    OR IS_KEY_PRESSED VK_KEY_S
    OR IS_KEY_PRESSED VK_KEY_A
    OR IS_KEY_PRESSED VK_KEY_D
        TASK_WARP_CHAR_INTO_CAR_AS_DRIVER scplayer, veh
        TASK_PLAY_ANIM scplayer, CAR_tune_radio, ped, 4.0, 0, 0, 0, 0, -1
        WAIT 300
        SET_AUDIO_STREAM_STATE autopilotoff, 1
        SET_AUDIO_STREAM_VOLUME autopilotoff, sfxvolume
        autopilot = FALSE
    ENDIF
ENDIF

IF IS_KEY_PRESSED VK_KEY_C
AND IS_KEY_PRESSED VK_KEY_X
AND IS_CHAR_IN_ANY_CAR scplayer
    mode += 1
    IF mode > 5
        mode = 0
    ENDIF
    SET_CAR_DRIVING_STYLE veh, mode
    SWITCH mode
        CASE 0
            PRINT_FORMATTED_NOW "Modo do Autopilot: Respeita semaforos, freia para obstaculos", 3000
            BREAK
        CASE 1
            PRINT_FORMATTED_NOW "Modo do Autopilot: Respeita semaforos, dirige sobre obstaculos", 3000
            BREAK
        CASE 2
            PRINT_FORMATTED_NOW "Modo do Autopilot: Ignora semaforos, contorna obstaculos", 3000
            BREAK
        CASE 3
            PRINT_FORMATTED_NOW "Modo do Autopilot: Ignora semaforos, dirige sobre obstaculos", 3000
            BREAK
        CASE 4
            PRINT_FORMATTED_NOW "Modo do Autopilot: Ignora semaforos, freia para obstaculos", 3000
            BREAK
        CASE 5
            PRINT_FORMATTED_NOW "Modo do Autopilot: Respeita semaforos, contorna obstaculos", 3000
            BREAK
    ENDSWITCH
    WHILE IS_KEY_PRESSED VK_KEY_C
    AND IS_KEY_PRESSED VK_KEY_X
        WAIT 0
    ENDWHILE
ENDIF

IF IS_KEY_PRESSED VK_KEY_X
AND NOT IS_KEY_PRESSED VK_KEY_C
AND IS_CHAR_IN_ANY_CAR scplayer
    GET_RADIO_CHANNEL ivar
    SET_RADIO_CHANNEL 12
    WHILE IS_KEY_PRESSED VK_KEY_X
    AND NOT IS_KEY_PRESSED VK_KEY_C
        WAIT 100
        IF IS_MOUSE_WHEEL_UP
            autospeed +=@ 5.0
        ENDIF
        IF IS_MOUSE_WHEEL_DOWN
            autospeed -=@ 5.0
        ENDIF
        SET_RADIO_CHANNEL 12
        IF IS_KEY_PRESSED VK_KEY_W
        AND IS_KEY_PRESSED VK_KEY_S
            autospeed = 20.0
        ENDIF
        IF autospeed < 0.0
            autospeed = 0.0
        ENDIF
        PRINT_FORMATTED_NOW "Velocidade de cruzeiro do autopilot: %.0f", 500, autospeed
    ENDWHILE
    WAIT 200
    SET_RADIO_CHANNEL ivar
ENDIF

IF IS_KEY_PRESSED VK_KEY_Z
AND NOT autodrive = TRUE
AND IS_CHAR_IN_ANY_CAR scplayer
    IF autopilot = FALSE
        TASK_PLAY_ANIM scplayer, CAR_tune_radio, ped, 4.0, 0, 0, 0, 0, -1
        WAIT 300
        SET_AUDIO_STREAM_STATE autopiloton, 1
        SET_AUDIO_STREAM_VOLUME autopiloton, sfxvolume
        IF GET_TARGET_BLIP_COORDS x, y, z
            GET_GROUND_Z_FOR_3D_COORD x, y, z, z
            GET_CLOSEST_CAR_NODE x, y, z, x, y, z
            TASK_CAR_DRIVE_TO_COORD scplayer, veh, x, y, z, autospeed, 0, 0, 0
        ELSE
            //SET_CAR_RANDOM_ROUTE_SEED veh, rnd // ?
            TASK_CAR_DRIVE_WANDER scplayer, veh, autospeed, 1
        ENDIF
        SET_CAR_CAN_GO_AGAINST_TRAFFIC veh, 0
        SET_CAR_DRIVING_STYLE veh, mode
        autopilot = TRUE
        //TASK_PLAY_ANIM scplayer, CAR_sitp, ped, 4.0, 1, 0, 0, 1, 10000 // Faz o autopilot parar
    ELSE
        TASK_WARP_CHAR_INTO_CAR_AS_DRIVER scplayer, veh
        TASK_PLAY_ANIM scplayer, CAR_tune_radio, ped, 4.0, 0, 0, 0, 0, -1
        WAIT 300
        SET_AUDIO_STREAM_STATE autopilotoff, 1
        SET_AUDIO_STREAM_VOLUME autopilotoff, sfxvolume
        autopilot = FALSE
    ENDIF
ENDIF

IF autopilot = TRUE
AND FRAME_MOD 40
    IF GET_TARGET_BLIP_COORDS x, y, z
    OR autodrive = TRUE
        IF GOSUB checkvehicle
            GOTO main_loop
        ENDIF
        IF autodrive = TRUE
            SET_CAR_DRIVING_STYLE veh, 2
            GET_CHAR_COORDINATES scplayer, x, y, z
        ELSE
            SET_CAR_DRIVING_STYLE veh, mode
        ENDIF
        GET_GROUND_Z_FOR_3D_COORD x, y, z, z
        GET_CLOSEST_CAR_NODE x, y, z, x, y, z
        IF autodrive = TRUE
            TASK_CAR_DRIVE_TO_COORD char2, veh, x, y, z, 20.0, 0, 0, 0
        ELSE
            TASK_CAR_DRIVE_TO_COORD scplayer, veh, x, y, z, autospeed, 0, 0, 0
        ENDIF
        SET_CAR_CAN_GO_AGAINST_TRAFFIC veh, 0
        x2 = x + -10.0
        y2 = y + -10.0
        x += 10.0
        y += 10.0
        IF NOT IS_CAR_IN_AREA_2D veh, x, y, x2, y2, 0
        AND autodrive = TRUE
            GET_CHAR_COORDINATES scplayer, x, y, z
            x2 = x + -10.0
            y2 = y + -10.0
            x += 10.0
            y += 10.0
        ENDIF
        IF IS_CAR_IN_AREA_2D veh, x, y, x2, y2, 0
            IF autodrive = TRUE
                SET_CAR_FORWARD_SPEED veh, 0.0
                SET_CHAR_MODEL_ALPHA char2, 255
                DELETE_CHAR char2
                GET_DOOR_ANGLE_RATIO veh, 2, fvar
                WHILE TRUE
                    WAIT 0
                    IF GOSUB checkvehicle
                        GOTO main_loop
                    ENDIF
                    fvar +=@ 0.01
                    CAR_HORN veh // Não funciona aqui?
                    SET_CAR_LIGHTS_ON veh, 1 // Same
                    OPEN_CAR_DOOR_A_BIT veh, 2, fvar
                    IF IS_CAR_DOOR_FULLY_OPEN veh, 2
                        BREAK
                    ENDIF
                ENDWHILE
                SET_CAR_LIGHTS_ON veh, 0
                SET_CAR_ENGINE_ON veh, 0
            ELSE
                TASK_WARP_CHAR_INTO_CAR_AS_DRIVER scplayer, veh
                SET_AUDIO_STREAM_STATE autopilotoff, 1
                SET_AUDIO_STREAM_VOLUME autopilotoff, sfxvolume
                APPLY_BRAKES_TO_PLAYERS_CAR 0, 1
                WAIT 1000
                APPLY_BRAKES_TO_PLAYERS_CAR 0, 0
            ENDIF
            autopilot = FALSE
            autodrive = FALSE
        ENDIF
    ENDIF
ENDIF

GOTO main_loop

checkvehicle:
IF NOT DOES_VEHICLE_EXIST veh
OR IS_CAR_DEAD veh
    autopilot = FALSE
    autodrive = FALSE
    RETURN_TRUE
ELSE
    RETURN_FALSE
ENDIF
RETURN

}
SCRIPT_END