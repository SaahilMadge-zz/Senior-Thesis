import numpy as np
import theano
import theano.tensor as T

# Our data vectors
X = T.matrix('X') # matrix of doubles
y = T.lvector('y') # vector of int64

nn_input_dim =
nn_output_dim =
nn_state_dim =
# nn_update_dim = # this is for f_t, i_t, c_tilde_t
nn_comb_input_dim = nn_input_dim + nn_state_dim
nn_idim =
nn_ctdim =


# Define our shared variables
Wf = theano.shared(np.random.randn(nn_comb_input_dim, nn_state_dim), name = 'Wf')
bf = theano.shared(np.zeros(nn_state_dim), name = 'bf')
Wi = theano.shared(np.random.randn(nn_comb_input_dim, nn_state_dim), name = 'Wi')
bi = theano.shared(np.zeros(nn_state_dim), name = 'bi')
Wc = theano.shared(np.random.randn(nn_comb_input_dim, nn_state_dim), name = 'Wc')
bc = theano.shared(np.zeros(nn_state_dim), name = 'bc')
C_tprev = **********
Wo = theano.shared(np.random.randn(nn_comb_input_dim, nn_state_dim), name = 'Wo')
bo = theano.shared(np.zeros(nn_state_dim), name = 'bo')

# Forward propagation
comb_input = T.concatenate([X, h_t_prev], axis=1)
z_f_t = comb_input.dot(Wf) + bf
f_t = T.nnet.sigmoid(z_f_t)
z_i_t = comb_input.dot(Wi) + bi
i_t = T.nnet.sigmoid(z_i_t)
z_c_tilde_t = comb_input.dot(Wc) + bc
c_tilde_t = T.tanh(z_c_tilde_t)
c_t = f_t * c_t_prev + i_t * c_tilde_t
z_o_t = comb_input.dot(Wo) + bo
o_t = T.nnet.sigmoid(z_o_t)
h_t = o_t * T.tanh(c_t)
