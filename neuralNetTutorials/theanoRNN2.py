import numpy as np
import theano
import theano.tensor as T

# RE-IMPLEMENT A RECURRENT NEURAL NETWORK
nn_input_dim = 7
nn_hdim = 10
nn_output_dim = 7

# function to initialize weight matrix of given size
def initialize_weight(in_size, out_size):
    return theano.shared(np.random.randn(in_size, out_size))

# function to initialize all params given dimensions
def initialize_params(in_dim, h_dim, out_dim):
    W_ih = initialize_weight(in_dim, h_dim)
    W_hh = initialize_weight(h_dim, h_dim)
    b_ih = theano.shared(np.zeros(h_dim))
    W_ho = initialize_weight(h_dim, out_dim)
    b_ho = theano.shared(np.zeros(out_dim))
    h_0 = theano.shared(np.zeros(h_dim))
    return W_ih, W_hh, b_ih, W_ho, b_ho, h_0

# initialize all our parameters
W_ih, W_hh, b_ih, W_ho, b_ho, h_0 = initialize_params(nn_input_dim, nn_hdim,
                                                    nn_output_dim)
params = [W_ih, W_hh, b_ih, W_ho, b_ho, h_0]

X = T.matrix('X')
y = T.matrix('y')

# function to do one step in the RNN
def one_step(x, h_t_prev, W_ih, W_hh, b_ih, W_ho, b_ho):
    z_ih = x.dot(W_ih) + h_t_prev.dot(W_hh) + b_ih
    h_t = T.tanh(z_ih)
    z_o = h_t.dot(W_ho) + b_ho
    cur_output = T.nnet.softmax(z_o).dimshuffle(1,)
    return [h_t, cur_output]

[hidden_states, y_hat_matrix], updates = theano.scan(fn=one_step,
                                                     sequences=X,
                                                     outputs_info=[h_0, None],
                                                     non_sequences=params[:-1])
# define learning rate
epsilon = 0.04
loss = T.nnet.categorical_crossentropy(y_hat_matrix, y).mean()

# define gradient function
def get_train_functions(X, target, loss):
    gradient_params = []
    for i in xrange(len(params)):
    # for param in params:
        param = params[i]
        gradient_param = T.grad(loss, param)
        gradient_params.append(gradient_param)

    updates_array = []
    for (param, gparam) in zip(params, gradient_params):
        updates_array.append((param, param - gparam * epsilon))

    learn_rnn_fn = theano.function(inputs=[X, target], outputs=loss,
                                   updates=updates_array)
    return learn_rnn_fn

learn_rnn_fn = get_train_functions(X, y, loss)

import reberGrammar
train_data = reberGrammar.get_n_examples(1000)

def train_model(train_data, epochs=50):
    train_errors = []
    for i in xrange(epochs):
        error = 0
        for j in range(len(train_data)):
            index = np.random.randint(0, len(train_data))
            i, o = train_data[index]
            train_cost = learn_rnn_fn(i, o)
            error += train_cost
        train_errors.append(error)
    return train_errors

print train_model(train_data)
