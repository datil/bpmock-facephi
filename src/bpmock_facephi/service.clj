(ns bpmock-facephi.service
  (:require [bpmock-facephi.response :as res]
            [bpmock-facephi.schema :as schema]
            [cheshire.core :as json]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor.helpers :as interceptor]
            [pedestal.swagger.core :as swagger]
            [pedestal.swagger.doc :as swagger.doc]
            [ring.util.response :as ring-resp]
            [schema.core :as s]))

(defn annotate
  "Adds metatata m to a swagger route"
  [m]
  (swagger.doc/annotate m (interceptor/before ::annotate identity)))

(swagger/defhandler get-device
  {:summary "Consulta si un dispositivo está registrado."
   :description "La consulta se realiza en el API de Movilmático."
   :parameters {:path schema/GetDeviceRequestPathParams}
   :responses {200 {:description "El dispositivo está registrado."
                    :schema schema/GetDeviceResponse}
               404 {:description "El dispositivo no está registrado."
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params] :as request}]
  (if (= (:fingerprint path-params) "001")
    (res/ok {:fingerprint "001"
             :type "tablet"})
    (res/not-found {:message "Dispositivo no registrado."
                    :code "not_found"})))

(swagger/defhandler get-username
  {:summary "Consulta si un usuario está matriculado en autenticación biométrica."
   :description "La consulta se hace al servicio de autenticación del banco."
   :parameters {:path schema/GetUsernameRequestPathParams
                :header schema/AuthenticatedRequestHeader}
   :responses {200 {:description "El usuario está matriculado."
                    :schema schema/GetUsernameResponse}
               404 {:description "El usuario no está matriculado"
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params] :as request}]
  (Thread/sleep 600)
  (case (:username path-params)
    "rosaaviles1604" (res/ok
                      {:username "rosaaviles1604"
                       :blocked false
                       :devices [{:fingerprint "ABCDE"
                                  :type "tablet"
                                  :created "2015-10-06T21:55:59Z"
                                  :description "iPad"}
                                 {:fingerprint "DECEE"
                                  :type "smartphone"
                                  :created "2015-10-06T21:55:59Z"
                                  :description "Nexus"}]})
    "dschuldt" (res/ok
                {:username "dschuldt"
                 :blocked true
                 :devices [{:fingerprint "XYZ"
                            :type "smartphone"
                            :created "2015-10-06T21:55:59Z"
                            :description "iPhone"}]})
    (res/not-found
     {:message "El usuario no está enrolado en FacePhi Service."
      :code "not_found"})))

(swagger/defhandler register-device
  {:summary "Registra un nuevo dispositivo."
   :description "Esta llamada asume que el usuario ya está matriculado en
                 autenticación biométrica. La respuesta es vacía."
   :parameters {:path schema/RegisterDeviceRequestPathParams
                :body schema/RegisterDeviceRequest
                :header schema/AuthenticatedRequestHeader}
   :responses {201 {:description "El dispositivo se registró con éxito."}
               409 {:description "Ya existe un dispositivo de ese tipo registrado."
                    :schema schema/ErrorResponse}
               403 {:description "La llave de sesión es inválida."
                    :schema schema/ErrorResponse}
               404 {:description "El usuario no existe."
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params] :as request}]
  (Thread/sleep 600)
  (case (:username path-params)
    "rosaaviles1604" (res/created
                      {:message "El dispositivo fue creado con éxito."})
    "dschuldt" (res/conflict
                {:message "Este tipo de dispositivo ya existe."
                 :code "conflict"})
    (res/forbidden
     {:message "Su sesión no está autorizada para acceder este recurso."
      :code "forbidden"})))

(swagger/defhandler replace-device
  {:summary "Reemplaza un dispositivo."
   :description "El dispositivo nuevo está representado en el cuerpo del pedido.
                 El dispositivo a reemplazar se referencia con su 'fingerprint'
                 y el usuario respectivo en la URL."
   :parameters {:path schema/ReplaceDeviceRequestPathParams
                :body schema/ReplaceDeviceRequest
                :header schema/AuthenticatedRequestHeader}
   :responses {200 {:description "El dispositivo fue reemplazado con éxito."}
               404 {:description "El dispositivo o usuario no existen."
                    :schema schema/ErrorResponse}
               403 {:description "La llave de sesión es inválida"
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params] :as request}]
  (Thread/sleep 600)
  (case (:username path-params)
    "rosaaviles1604" (res/ok
                      {:message "El dispositivo fue actualizado con éxito."})
    "dschuldt" (res/not-found
                {:message "El dispositivo no existe."
                 :code "not_found"})
    (res/forbidden
     {:message "Su sesión no está autorizada para acceder este recurso."
      :code "forbidden"})))

(swagger/defhandler register-user
  {:summary "Matricula un nuevo usuario en autenticación biométrica."
   :description "Crea un nuevo dispositivo y registra al usuario en el servicio
                 de autenticación biométrica."
   :parameters {:body schema/RegisterUserRequest
                :header schema/AuthenticatedRequestHeader}
   :responses {201 {:description "El usuario fue matriculado con éxito."}
               400 {:description "Los datos son inválidos."
                    :schema schema/ErrorResponse}
               409 {:description "El usuario ya está registrado."
                    :schema schema/ErrorResponse}
               401 {:description "El código OTP es incorrecto."
                    :schema schema/ErrorResponse}
               403 {:description "La llave de sesión es inválida."
                    :schema schema/ErrorResponse}}}
  [{:keys [body-params] :as request}]
  (case (:username body-params)
    "rosaaviles1604" (res/created
                      {:username "rosaaviles1604"
                       :created "2015-10-13T15:11:11Z"
                       :last_updated "2015-10-13T15:11:11Z"
                       :is_active 1
                       :identification "0914617584"
                       :devices [{:type "smartphone"
                                  :fingerprint "ABCDE"
                                  :description "iPhone"
                                  :created "2015-10-13T15:11:11Z"}]})
    "raviles1964" (res/created
                      {:username "raviles1964"
                       :created "2015-10-13T15:11:11Z"
                       :last_updated "2015-10-13T15:11:11Z"
                       :is_active 1
                       :identification "0914617584"
                       :devices [{:type "smartphone"
                                  :fingerprint "ABCDE"
                                  :description "iPhone"
                                  :created "2015-10-13T15:11:11Z"}]})
    "dschuldt" (res/bad-request
                {:message "El usuario ya está registrado."
                 :code "bad_request"})
    "scarface" (res/bad-request
                {:message "Los datos biométricos no son válidos."
                 :code "bad_request"})
    (res/unauthorized
     {:message "El OTP no es válido."
      :code "unauthorized"})))

(swagger/defhandler authenticate-user
  {:summary "Autentica a un usuario con su dispositivo y perfil biométrico."
   :description "El 'fingerprint' del dispositivo equivale al usuario y el
                 perfil biométrico a la contraseña."
   :parameters {:body schema/AuthenticateRequest}
   :responses {200 {:description "El usuario fue autenticado"
                    :schema schema/AuthenticateResponse}
               401 {:description "El usuario no fue autenticado"
                    :schema schema/ErrorResponse}}}
  [{:keys [body-params] :as request}]
  (case (:fingerprint body-params)
    "1234" (res/ok
            {:customer
             {:status "True"
              :transaction-cost "0.35"
              :last-access "10/4/2015 9:24:09 AM"
              :unknown-field "1"
              :visit-number "1234567"
              :identification "0914617584"
              :legal-entity-type "N"
              :visitor-id "1234567890"
              :contract-number "41"
              :concurrency-token "12345678901234567890123456789012"
              :query-cost "0.00"
              :username "rosaaviles1604"
              :email "e@datil.co"
              :code "0000"
              :rate-code "0"
              :bpapp-session-token "21f75920-6aa3-11e5-8825"
              :customer-name "JIMENEZ PITA MANUEL"}})
    "4565" (res/locked
            {:code "locked"
             :message "El uso de reconocimiento facial está bloqueado temporalmente. Por favor, ingrese con su usuario y contraseña para desbloquearlo."})
    (res/unauthorized
     {:message "El dispositivo o perfil biométrico no son correctos."
      :code "unauthorized"})))

(swagger/defhandler retrain-user
  {:summary "Reentrena el perfil biométrico de un usuario."
   :description "El reentrenamiento está restringido a usuarios autenticados
                 con usuario y contraseña."
   :parameters {:path schema/RetrainRequestPathParams
                :body schema/RetrainRequest
                :header schema/AuthenticatedRequestHeader}
   :responses {200 {:description "El perfil se re-entrenó con éxito."}
               400 {:description "Los datos son inválidos."
                    :schema schema/ErrorResponse}
               403 {:description "La llave de sesión no es válida."
                    :schema schema/ErrorResponse}
               404 {:description "El usuario no está matriculado."
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params body-params] :as request}]
  (case [(:username path-params) (:password body-params)]
    ["rosaaviles1604" "123"] (res/ok
                              {:username "rosaaviles1604"})
    (res/unauthorized
     {:message "El usuario o contraseña no es válido."
      :code "unauthorized"})))

(swagger/defhandler send-otp
  {:summary "Envía un código OTP al usuario."
   :description "El envío está restringido a usuarios autenticados con
                 usuario y contraseña."
   :parameters {:path schema/SendOTPRequestPathParams
                :header schema/AuthenticatedRequestHeader}
   :responses {200 {:description "El OTP fué enviado con éxito."}
               403 {:description "La llave de sesión no es válida."
                    :schema schema/ErrorResponse}}}
  [{:keys [path-params] :as request}]
  (case (:username path-params)
    "rosaaviles1604" (res/ok
                      {:otp "sent"})
    (res/forbidden
     {:message "Su sesión no está autorizada para acceder este recurso."
      :code "forbidden"})))

(defn accounts
  [request]
  (res/ok {:accounts [{:description "1063365620 Cta. Ahorros",
                       :type-label "Cta. Ahorros",
                       :type "10",
                       :available-balance "475.96",
                       :accounting-balance "475.96",
                       :customer-name "MARTINEZ HEISENBERG",
                       :number "1063365620",
                       :id "1"}]
           :credit-cards []
           :loans        []
           :investments  []}))

(defn detectid
  [request]
  (res/ok {:detectid-image {:username (get-in request [:params :username])
                            :id "2233442"
                            :description "Estrellita donde estás."
                            :uri "/iso.png"}}))
(defn get-customer
  [request]
  (res/ok {:customer
           {:status "True"
            :transaction-cost "0.35"
            :last-access "10/4/2015 9:24:09 AM"
            :unknown-field "1"
            :visit-number "1234567"
            :identification "0914617584"
            :legal-entity-type "N"
            :visitor-id "1234567890"
            :contract-number "41"
            :concurrency-token "12345678901234567890123456789012"
            :query-cost "0.00"
            :username (get-in request [:params :username])
            :email "e@datil.co"
            :code "0000"
            :rate-code "0"
            :bpapp-session-token "21f75920-6aa3-11e5-8825"
            :customer-name "JIMENEZ PITA MANUEL"}}))


(swagger/defroutes routes
  {:info {:title "bpmock-facephi"
          :description "Simulador de servicio de autenticación biométrica anexo
                        a bpapp-api."
          :version "1.0.0"}}
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/"
     ^:interceptors [bootstrap/json-body
                     (swagger/body-params)
                     (swagger/coerce-request)]
     ["/customers" {:get [:get-customer get-customer]}
      ["/:username/otp" {:post [:send-otp send-otp]}]]
     ["/accounts" {:get [:accounts accounts]}]
     ["/detectid-images" {:get [:detectid detectid]}]
     ["/facephi"
      ["/authentication" {:post [:authenticate-user authenticate-user]}]
      ["/devices/:fingerprint" {:get get-device}]
      ["/users" {:post [:register-user register-user]}
       ["/:username" {:get [:get-username get-username]}
        ["/retrain" {:post [:retrain-user retrain-user]}]
        ["/devices/registration" {:post [:register-device register-device]}]
        ["/devices/:fingerprint/replacement" {:post [:replace-device replace-device]}]]]]
     ["/doc" {:get [(swagger/swagger-json)]}]
     ["/*resource" {:get [(swagger/swagger-ui)]}]]]])

;; Consumed by bpmock-facephi.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes
              ::bootstrap/router :linear-search
              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
