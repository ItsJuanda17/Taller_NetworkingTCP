# Taller_NetworkingTCP

## Integrantes

- **Juan David Acevedo** - A00399081
- **Alejandro Quiñones** - A00377013


# Sistema de Chat Multisalas con Soporte para Llamadas de Voz

### Descipción: 
Este proyecto es una aplicación de chat multisalas que permite la comunicación entre clientes conectados a un servidor. Los usuarios pueden intercambiar mensajes de texto tanto en grupo como de manera privada, y realizar llamadas de voz. Además, se pueden crear salas de chat donde los usuarios pueden unirse para enviar mensajes y comunicarse por voz.

## Funcionalidades Principales

1. **Sala Común:**
   - Cuando los usuarios se conectan, el servidor automáticamente los añade a una sala común creada por el servidor. 
   - En esta sala, los usuarios pueden enviar:
     - **Mensajes públicos**: Todos los usuarios en la sala común reciben estos mensajes.
     - **Mensajes privados**: Los usuarios pueden enviar mensajes directos a otro usuario específico.
     - **Mensajes de voz**: Los usuarios pueden enviar mensajes de voz que serán escuchados por todos los usuarios en la sala común.

2. **Salas Personalizadas:**
   - Dentro de la sala común, los usuarios tienen la posibilidad de crear nuevas **salas** personalizadas, de manera similar a la funcionalidad de "Breakout Rooms" en Zoom.
   - Los usuarios pueden enviar mensajes públicos o mensajes de voz dentro de estas salas personalizadas.
   - Cada sala mantiene su propio **historial de mensajes**, que puede ser consultado en cualquier momento.

3. **Llamadas:**
   - La aplicación soporta la funcionalidad de **llamadas**, en las que todos los usuarios conectados a la sala pueden escuchar.
   - Hasta el momento, las llamadas son escuchadas por todos los usuarios conectados a la sala a la que pertenece el usuario que inició la llamada.

4. **Lista de Salas Disponibles:**
   - Cuando un usuario desea unirse a una sala, el `ClientHandler` se encarga de enviar una lista de las salas disponibles con sus nombres.
   - El usuario puede entonces ingresar el nombre de la sala a la que desea unirse.


## Clases Principales

### 1. `ClientHandler`

La clase `ClientHandler` es la encargada de manejar la conexión con cada cliente. Permite realizar las siguientes operaciones:

- **Mensajes privados**: Los clientes pueden enviar mensajes privados a otros usuarios utilizando el comando `PRIVATE <destinatario> <mensaje>`.
- **Mensajes en salas**: Los clientes pueden enviar mensajes a una sala de chat utilizando el comando `ROOM_MSG <mensaje>`.
- **Llamadas de voz**: Los usuarios pueden iniciar llamadas de voz a otros usuarios o a todos los usuarios en una sala. El comando para realizar una llamada es `CALL <usuario>`.
- **Administración de salas**: Los usuarios pueden crear salas nuevas con el comando `CREATE_ROOM <nombre_sala>` y unirse a salas existentes con `JOIN_ROOM <nombre_sala>`.
- **Salir del chat**: Los usuarios pueden desconectarse utilizando el comando `EXIT`, que cierra la conexión.

### 2. `Client`

La clase `Client` representa al cliente que se conecta al servidor. Este cliente puede realizar las siguientes acciones:

- **Enviar mensajes al grupo**: Se envía un mensaje a todos los usuarios conectados.
- **Enviar mensajes privados**: Se envía un mensaje a un usuario específico.
- **Grabar y enviar audio**: El cliente puede grabar audio y enviarlo a otros usuarios o a una sala.
- **Crear y unirse a salas**: El cliente puede crear una nueva sala o unirse a una existente.
- **Realizar llamadas de voz**: Los usuarios pueden realizar llamadas de voz individuales o en salas.
- **Finalizar llamadas**: Los usuarios pueden finalizar una llamada utilizando el comando `END_CALL`.

### 3. `Chatters`
Esta clase gestiona la lógica del servidor. Almacena a los usuarios conectados, las salas de chat, y el historial de los mensajes. Las funcionalidades clave incluyen:

- **Gestión de usuarios**: Añadir o eliminar usuarios de la sala común y de salas personalizadas.
- **Mensajes globales y privados**: Envío de mensajes a todos los usuarios o a un usuario específico.
- **Salas de chat**: Creación de nuevas salas, añadir usuarios a las salas y envío de mensajes a una sala específica.
- **Historial de mensajes**: Almacena el historial de cada sala de chat.
- **Mensajes de voz**: Los usuarios pueden enviar mensajes de voz tanto a la sala común como a salas personalizadas.
### Instrucciones de uso:

- **Ejecutar el servidor:** El servidor debe estar ejecutándose para aceptar conexiones de los clientes. 

- **Ejecutar el cliente:** Una vez que el servidor esté ejecutándose, ejecuta el cliente o los clientes, el servidor reicbe multiples conexiones.

- **Interacción con el sistema:** El cliente solicitará un nombre de usuario al conectarse. Aparecerá un menú con las opciones disponibles para enviar mensajes, crear salas, unirse a ellas, y realizar llamadas. Puedes seguir las opciones del menú para interactuar con otros usuarios.

***Tener en cuenta*** : Cada opción en el menú está asociada a una funcionalidad específica. Por ejemplo, si deseas enviar un mensaje a la sala, debes utilizar la opción "Send message to room" en lugar de "Send message". Aunque estas funcionalidades son similares, son diferentes en su aplicación: la primera se refiere al grupo general creado por el servidor cuando los clientes se conectan, mientras que la segunda se utiliza cuando un cliente crea una sala para comunicarse de manera específica dentro de esa sala. Esa información esta recalcada en el menú de opciones.

