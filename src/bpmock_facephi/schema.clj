(ns bpmock-facephi.schema
  (:require [schema.core :as s]))

(def req s/required-key)
(def opt s/optional-key)

(s/defschema SupportedDeviceTypes
  (s/enum :tablet :smartphone))

(s/defschema ErrorResponse
  {(req :message) s/Str
   (req :code) s/Str})

(s/defschema GetDeviceRequestPathParams
  {(req :fingerprint) s/Str})

(s/defschema GetDeviceResponse
  {(req :fingerprint) s/Str
   (req :created) s/Str
   (req :type) SupportedDeviceTypes
   (req :description) s/Str})

(s/defschema GetUsernameRequestPathParams
  {(req :username) s/Str})

(s/defschema GetUsernameResponse
  {(req :username) s/Str
   (req :devices) [{(req :type) SupportedDeviceTypes
                    (req :created) s/Str
                    (req :description) s/Str}]})

(s/defschema AuthenticatedRequestHeader
  {(req "bpapp-session-token") s/Str})

(s/defschema RegisterDeviceRequestPathParams
  {(req :username) s/Str})

(s/defschema RegisterDeviceRequest
  {(req :fingerprint) s/Str
   (req :type) SupportedDeviceTypes
   (req :description) s/Str})

(s/defschema ReplaceDeviceRequestPathParams
  {(req :username) s/Str
   (req :fingerprint) s/Str})

(s/defschema ReplaceDeviceRequest
  {(req :fingerprint) s/Str
   (req :type) SupportedDeviceTypes
   (req :description) s/Str})

(s/defschema DeactivateDeviceRequestPathParams
  {(req :username) s/Str
   (req :fingerprint) s/Str})

(s/defschema RegisterUserRequest
  {(req :username) s/Str
   (req :device) {(req :fingerprint) s/Str
                  (req :type) SupportedDeviceTypes
                  (req :description) s/Str}
   (req :templates) [s/Str]
   (req :otp) s/Str})

(s/defschema AuthenticateRequest
  {(req :fingerprint) s/Str
   (req :template) s/Str})

(s/defschema AuthenticateResponse
  {(req :customer) {(req :status) s/Str
                    (req :transaction-cost) s/Str
                    (req :last-access) s/Str
                    (req :visit-number) s/Str
                    (req :unknown-field) s/Str
                    (req :visitor-id) s/Str
                    (req :contract-number) s/Str
                    (req :concurrency-token) s/Str
                    (req :query-cost) s/Str
                    (req :username) s/Str
                    (req :code) s/Str
                    (req :identification) s/Str
                    (req :email) s/Str
                    (req :rate-code) s/Str
                    (req :legal-entity-type) (s/enum :N :S)
                    (req :bpapp-session-token) s/Str
                    (req :customer-name) s/Str}})

(s/defschema RetrainRequestPathParams
  {(req :username) s/Str})

(s/defschema RetrainRequest
  {(req :password) s/Str
   (req :template) s/Str})

(s/defschema SendOTPRequestPathParams
  {(req :username) s/Str})
